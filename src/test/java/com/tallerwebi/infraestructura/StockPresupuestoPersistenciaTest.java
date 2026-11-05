package com.tallerwebi.infraestructura;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import com.tallerwebi.presentacion.ItemPresupuestoForm;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(SpringExtension.class)
@org.springframework.test.context.ActiveProfiles("stock-presupuesto-test")
@ContextConfiguration(classes = {
  HibernateInfraestructuraTestConfig.class, StockPresupuestoPersistenciaTest.Servicios.class
})
public class StockPresupuestoPersistenciaTest {

  private static final AtomicInteger CODIGOS = new AtomicInteger(800000);

  @Autowired
  private ServicioPresupuesto presupuesto;
  @Autowired
  private ServicioOrdenReparacion servicioOrden;
  @Autowired
  private RepositorioOrdenReparacion ordenes;
  @Autowired
  private RepositorioRepuesto repuestos;
  @Autowired
  private PlatformTransactionManager transactionManager;

  private TransactionTemplate transaccion;
  private Long repuestoId;
  private Integer codigo;

  @BeforeEach
  public void preparar() {
    transaccion = new TransactionTemplate(transactionManager);
    codigo = CODIGOS.incrementAndGet();
    repuestoId = transaccion.execute(estado -> {
      Repuesto repuesto = new Repuesto("RAM", 1000.0, 5);
      repuestos.guardar(repuesto);
      crearOrden(codigo);
      return repuesto.getId();
    });
  }

  private void crearOrden(Integer codigoOrden) {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setCodigoSeguimiento(codigoOrden);
    orden.setEmailCliente("cliente@test.com");
    ordenes.guardarOrden(orden);
  }

  private void enviar(Integer codigoOrden, int cantidad) {
    presupuesto.enviarPresupuesto(codigoOrden,
      List.of(new ItemPresupuestoForm(repuestoId, cantidad)), 25000.0);
  }

  private int stock() {
    return transaccion.execute(estado -> repuestos.buscarPorId(repuestoId).getStock());
  }

  @Test
  public void rechazoEnOtraTransaccionDevuelveElDetallePersistidoUnaSolaVez() {
    enviar(codigo, 2);
    assertEquals(3, stock());
    transaccion.executeWithoutResult(estado -> {
      OrdenReparacion orden = ordenes.buscarPorCodigo(codigo);
      assertEquals(1, orden.getReservasPresupuesto().size());
      assertEquals(2, orden.getReservasPresupuesto().get(0).getCantidad());
    });
    servicioOrden.rechazarPresupuesto(codigo, "cliente@test.com");
    assertEquals(5, stock());
    assertThrows(IllegalStateException.class,
      () -> servicioOrden.rechazarPresupuesto(codigo, "cliente@test.com"));
    assertEquals(5, stock());
  }

  @Test
  public void errorRevierteStockEstadoYReservasEnLaMismaTransaccion() {
    assertThrows(IllegalStateException.class, () -> transaccion.executeWithoutResult(estado -> {
      enviar(codigo, 2);
      throw new IllegalStateException("Simulación de fallo posterior");
    }));
    assertEquals(5, stock());
    transaccion.executeWithoutResult(estado -> {
      OrdenReparacion orden = ordenes.buscarPorCodigo(codigo);
      assertEquals(EstadoOrden.RECIBIDO, orden.getEstado());
      assertTrue(orden.getReservasPresupuesto().isEmpty());
    });
  }

  @Test
  public void dosEnviosSimultaneosDeLaMismaOrdenSoloReservanUnaVez() throws Exception {
    verificarCarrera(codigo, codigo, 2);
    assertEquals(3, stock());
  }

  @Test
  public void dosOrdenesSimultaneasNoPuedenConsumirElMismoStock() throws Exception {
    Integer segundoCodigo = CODIGOS.incrementAndGet();
    transaccion.executeWithoutResult(estado -> crearOrden(segundoCodigo));
    verificarCarrera(codigo, segundoCodigo, 4);
    assertEquals(1, stock());
    transaccion.executeWithoutResult(estado -> {
      List<OrdenReparacion> participantes = List.of(
        ordenes.buscarPorCodigo(codigo), ordenes.buscarPorCodigo(segundoCodigo));
      assertEquals(1, participantes.stream()
        .filter(orden -> orden.getEstado() == EstadoOrden.PRESUPUESTO_ENVIADO).count());
      assertEquals(4, participantes.stream().flatMap(orden -> orden.getReservasPresupuesto().stream())
        .mapToInt(ReservaRepuesto::getCantidad).sum());
    });
  }

  private void verificarCarrera(Integer primero, Integer segundo, int cantidad) throws Exception {
    CountDownLatch lecturas = new CountDownLatch(2);
    try (ExecutorService ejecutor = Executors.newFixedThreadPool(2)) {
      Future<Boolean> envioPrimero = ejecutor.submit(() -> enviarEnParalelo(primero, cantidad, lecturas));
      Future<Boolean> envioSegundo = ejecutor.submit(() -> enviarEnParalelo(segundo, cantidad, lecturas));
      int exitosos = (envioPrimero.get(30, TimeUnit.SECONDS) ? 1 : 0)
        + (envioSegundo.get(30, TimeUnit.SECONDS) ? 1 : 0);
      assertEquals(1, exitosos);
    }
  }

  @Test
  public void edicionDeInventarioDesactualizadaNoSobrescribeUnaReserva() {
    Repuesto formularioViejo = transaccion.execute(estado -> repuestos.buscarPorId(repuestoId));
    enviar(codigo, 2);
    formularioViejo.setStock(5);
    assertThrows(org.springframework.dao.OptimisticLockingFailureException.class,
      () -> transaccion.executeWithoutResult(estado ->
        new ServicioInventarioImpl(repuestos).guardarOActualizar(formularioViejo)));
    assertEquals(3, stock());
    Repuesto formularioNuevo = transaccion.execute(estado -> repuestos.buscarPorId(repuestoId));
    formularioNuevo.setNombre("Nombre actualizado");
    transaccion.executeWithoutResult(estado ->
      new ServicioInventarioImpl(repuestos).guardarOActualizar(formularioNuevo));
    assertEquals(3, stock());
  }

  @Test
  public void detalleHistoricoNoCambiaCuandoSeEditaElCatalogo() {
    enviar(codigo, 2);
    transaccion.executeWithoutResult(estado -> {
      Repuesto repuesto = repuestos.buscarPorId(repuestoId);
      repuesto.setNombre("Otro nombre");
      repuesto.setPrecio(5000.0);
      repuestos.guardar(repuesto);
    });
    OrdenReparacion guardada = transaccion.execute(estado -> ordenes.buscarPorCodigo(codigo));
    ReservaRepuesto detalle = guardada.getReservasPresupuesto().get(0);
    assertEquals("RAM", detalle.getNombreRepuesto());
    assertEquals(new java.math.BigDecimal("1000.00"), detalle.getPrecioUnitario());
    assertEquals(new java.math.BigDecimal("2000.00"), detalle.getSubtotal());
    assertEquals(2000.0, guardada.getSubtotalRepuestos());
    assertEquals(20000.0, guardada.getCostoManoDeObra());
    assertEquals(22000.0, guardada.getMontoTotal());
  }

  private boolean enviarEnParalelo(Integer codigoOrden, int cantidad, CountDownLatch lecturas) {
    try {
      transaccion.executeWithoutResult(estado -> {
        ordenes.buscarPorCodigo(codigoOrden);
        repuestos.buscarPorId(repuestoId);
        lecturas.countDown();
        esperarLecturas(lecturas);
        enviar(codigoOrden, cantidad);
      });
      return true;
    } catch (org.springframework.dao.OptimisticLockingFailureException e) {
      return false;
    }
  }

  private void esperarLecturas(CountDownLatch lecturas) {
    try {
      assertTrue(lecturas.await(10, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }

  @Configuration
  @org.springframework.context.annotation.Profile("stock-presupuesto-test")
  static class Servicios {

    @Bean
    RepositorioRepuesto repuestos(org.hibernate.SessionFactory factory) {
      return new RepositorioRepuestoImpl(factory);
    }

    @Bean
    RepositorioOrdenReparacion ordenes(org.hibernate.SessionFactory factory) {
      return new RepositorioOrdenReparacionImpl(factory);
    }

    @Bean
    ServicioPresupuesto presupuesto(RepositorioRepuesto repuestos, RepositorioOrdenReparacion ordenes) {
      return new ServicioPresupuestoImpl(repuestos, ordenes);
    }

    @Bean
    ServicioOrdenReparacion servicioOrden(RepositorioOrdenReparacion ordenes,
      org.hibernate.SessionFactory factory) {
      return new ServicioOrdenReparacionImpl(new RepositorioUsuarioImpl(factory), ordenes);
    }
  }
}
