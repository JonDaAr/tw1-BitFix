package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.SinStockException;
import com.tallerwebi.presentacion.ItemPresupuestoForm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CicloStockPresupuestoTest {

  private RepositorioRepuesto repuestos;
  private RepositorioOrdenReparacion ordenes;
  private ServicioPresupuestoImpl presupuesto;
  private ServicioOrdenReparacionImpl servicioOrden;
  private OrdenReparacion orden;
  private Repuesto repuesto;

  @BeforeEach
  public void preparar() {
    repuestos = mock(RepositorioRepuesto.class);
    ordenes = mock(RepositorioOrdenReparacion.class);
    presupuesto = new ServicioPresupuestoImpl(repuestos, ordenes);
    servicioOrden = new ServicioOrdenReparacionImpl(mock(RepositorioUsuario.class), ordenes);
    orden = new OrdenReparacion();
    orden.setEmailCliente("cliente@test.com");
    repuesto = new Repuesto("RAM", 1000.0, 5);
    repuesto.setId(1L);
    when(ordenes.buscarPorCodigo(123456)).thenReturn(orden);
    when(repuestos.buscarPorId(1L)).thenReturn(repuesto);
  }

  private void enviar() {
    presupuesto.enviarPresupuesto(123456, List.of(new ItemPresupuestoForm(1L, 2)), 22000.0);
  }

  @Test
  public void rechazoDevuelveExactamenteLaReservaYNoPuedeRepetirse() {
    enviar();
    assertEquals(3, repuesto.getStock());
    servicioOrden.rechazarPresupuesto(123456, "cliente@test.com");
    assertEquals(5, repuesto.getStock());
    assertEquals(EstadoOrden.PRESUPUESTO_RECHAZADO, orden.getEstado());
    assertThrows(IllegalStateException.class,
      () -> servicioOrden.rechazarPresupuesto(123456, "cliente@test.com"));
    assertEquals(5, repuesto.getStock());
  }

  @Test
  public void aceptacionMantieneElDescuentoSinVolverADescontar() {
    enviar();
    servicioOrden.aceptarPresupuesto(123456, "cliente@test.com");
    assertEquals(3, repuesto.getStock());
    assertThrows(IllegalStateException.class, this::enviar);
    assertEquals(3, repuesto.getStock());
    assertThrows(IllegalStateException.class,
      () -> servicioOrden.rechazarPresupuesto(123456, "cliente@test.com"));
  }

  @Test
  public void reenvioTrasRechazoOPrimerEnvioNoDescuentaOtraVez() {
    enviar();
    assertThrows(IllegalStateException.class, this::enviar);
    assertEquals(3, repuesto.getStock());
    servicioOrden.rechazarPresupuesto(123456, "cliente@test.com");
    assertThrows(IllegalStateException.class, this::enviar);
    assertEquals(5, repuesto.getStock());
  }

  @Test
  public void itemsRepetidosSeAgrupanAntesDeValidarYReservar() {
    List<ItemPresupuestoForm> items = List.of(
      new ItemPresupuestoForm(1L, 2), new ItemPresupuestoForm(1L, 2)
    );
    presupuesto.enviarPresupuesto(123456, items, 24000.0);
    assertEquals(1, repuesto.getStock());
    assertEquals(1, orden.getReservasPresupuesto().size());
    assertEquals(4, orden.getReservasPresupuesto().get(0).getCantidad());
    verify(repuestos, times(1)).guardar(repuesto);
  }

  @Test
  public void faltaDeStockAcumuladoNoModificaNingunRepuesto() {
    Repuesto otro = new Repuesto("SSD", 1000.0, 10);
    when(repuestos.buscarPorId(2L)).thenReturn(otro);
    List<ItemPresupuestoForm> items = List.of(new ItemPresupuestoForm(2L, 1),
      new ItemPresupuestoForm(1L, 3), new ItemPresupuestoForm(1L, 3));
    assertThrows(SinStockException.class,
      () -> presupuesto.enviarPresupuesto(123456, items, 27000.0));
    assertEquals(10, otro.getStock());
    assertEquals(5, repuesto.getStock());
    assertEquals(EstadoOrden.RECIBIDO, orden.getEstado());
    assertTrue(orden.getReservasPresupuesto().isEmpty());
    verify(repuestos, never()).guardar(any());
  }

  @Test
  public void rechazoDeOtroClienteNoDevuelveStock() {
    enviar();
    assertThrows(SecurityException.class,
      () -> servicioOrden.rechazarPresupuesto(123456, "otro@test.com"));
    assertEquals(3, repuesto.getStock());
  }

  @Test
  public void cantidadesInvalidasNoIncrementanStock() {
    for (Integer cantidad : new Integer[] { null, 0, -2 }) {
      assertThrows(IllegalArgumentException.class,
        () -> presupuesto.descontarStockRepuesto(1L, cantidad));
    }
    assertThrows(IllegalArgumentException.class, () -> repuesto.descontarStock(-2));
    assertEquals(5, repuesto.getStock());
  }

  @Test
  public void presupuestoAntiguoSinDetalleNoInventaUnaDevolucion() {
    orden.setEstado(EstadoOrden.PRESUPUESTO_ENVIADO);
    servicioOrden.rechazarPresupuesto(123456, "cliente@test.com");
    assertEquals(5, repuesto.getStock());
    assertEquals(EstadoOrden.PRESUPUESTO_RECHAZADO, orden.getEstado());
  }
}
