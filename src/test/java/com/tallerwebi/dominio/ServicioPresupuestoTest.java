package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.SinStockException;
import com.tallerwebi.presentacion.ItemPresupuestoForm;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioPresupuestoTest {

  private RepositorioRepuesto repositorioRepuestoMock;
  private RepositorioOrdenReparacion repositorioOrdenMock;
  private ServicioPresupuesto servicioPresupuesto;

  @BeforeEach
  public void init() {
    this.repositorioRepuestoMock = mock(RepositorioRepuesto.class);
    this.repositorioOrdenMock = mock(RepositorioOrdenReparacion.class);

    this.servicioPresupuesto =
            new ServicioPresupuestoImpl(this.repositorioRepuestoMock, this.repositorioOrdenMock);
  }

  @Test
  public void dadoQueHayStock_cuandoSeCalculaSubtotal_entoncesDevuelveElMontoMultiplicado()
          throws SinStockException {
    Repuesto repuesto = new Repuesto("Memoria RAM 8GB", 20000.0, 5);
    when(this.repositorioRepuestoMock.buscarPorId(1L)).thenReturn(repuesto);

    Double subtotal = this.servicioPresupuesto.calcularSubtotalRepuesto(1L, 2);

    assertThat(subtotal, equalTo(40000.0));
    verify(this.repositorioRepuestoMock, times(1)).buscarPorId(1L);
  }

  @Test
  public void dadoQueNoHayStockSuficiente_quandoSeCalculaSubtotal_entoncesLanzaSinStockException() {
    Repuesto repuesto = new Repuesto("Placa de Video", 150000.0, 1);
    when(this.repositorioRepuestoMock.buscarPorId(2L)).thenReturn(repuesto);

    assertThrows(
            SinStockException.class,
            () -> {
              this.servicioPresupuesto.calcularSubtotalRepuesto(2L, 3);
            }
    );
  }

  @Test
  public void dadoQueHayStock_cuandoSeDescuenta_entoncesGuardaElRepuestoConMenorCantidad()
          throws SinStockException {
    Repuesto repuesto = new Repuesto("Pasta Térmica", 5000.0, 10);
    // Cambiado: Ahora el descuento llama al metodo con bloqueo
    when(this.repositorioRepuestoMock.buscarPorIdConBloqueo(3L)).thenReturn(repuesto);

    this.servicioPresupuesto.descontarStockRepuesto(3L, 4);

    assertThat(repuesto.getStock(), equalTo(6));
    verify(this.repositorioRepuestoMock, times(1)).guardar(repuesto);
  }

  @Test
  public void queAlCalcularTotalConVariosItemsRetorneLaSumaCorrecta() throws SinStockException {
    Repuesto ram = new Repuesto();
    ram.setId(1L);
    ram.setNombre("Memoria RAM 16GB");
    ram.setPrecio(38000.0);
    ram.setStock(5);

    Repuesto ssd = new Repuesto();
    ssd.setId(2L);
    ssd.setNombre("SSD NVMe 1TB");
    ssd.setPrecio(52000.0);
    ssd.setStock(3);

    when(repositorioRepuestoMock.buscarPorId(1L)).thenReturn(ram);
    when(repositorioRepuestoMock.buscarPorId(2L)).thenReturn(ssd);

    List<ItemPresupuestoForm> items = new ArrayList<>();
    items.add(new ItemPresupuestoForm(1L, 2));
    items.add(new ItemPresupuestoForm(2L, 1));

    Double total = servicioPresupuesto.calcularTotalPresupuesto(items);

    assertThat(total, equalTo(172800.0));
  }

  @Test
  public void queAlCalcularTotalConListaVaciaRetorneCero() throws SinStockException {
    List<ItemPresupuestoForm> itemsVacios = new ArrayList<>();

    Double total = servicioPresupuesto.calcularTotalPresupuesto(itemsVacios);

    assertThat(total, equalTo(0.0));
  }

  @Test
  public void queLanceSinStockExceptionSiAlgunItemSuperaElStockDisponible() {
    Repuesto pantalla = new Repuesto();
    pantalla.setId(3L);
    pantalla.setNombre("Pantalla OLED");
    pantalla.setPrecio(18000.0);
    pantalla.setStock(1);

    when(repositorioRepuestoMock.buscarPorId(3L)).thenReturn(pantalla);

    List<ItemPresupuestoForm> items = new ArrayList<>();
    items.add(new ItemPresupuestoForm(3L, 5));

    org.junit.jupiter.api.Assertions.assertThrows(
            SinStockException.class,
            () -> {
              servicioPresupuesto.calcularTotalPresupuesto(items);
            }
    );
  }

  @Test
  public void cuandoSubtotalRepuestosEsCero_laManoDeObraDebeSerCero() {
    Double manoDeObra = servicioPresupuesto.calcularManoDeObra(0.0);
    assertThat(manoDeObra, equalTo(0.0));
  }

  @Test
  public void cuandoSubtotalRepuestosEsMenorOIgualA50000_aplicaPisoFijoDe20000() {
    Double manoDeObraMenor = servicioPresupuesto.calcularManoDeObra(30000.0);
    Double manoDeObraExacto = servicioPresupuesto.calcularManoDeObra(50000.0);

    assertThat(manoDeObraMenor, equalTo(20000.0));
    assertThat(manoDeObraExacto, equalTo(20000.0));
  }

  @Test
  public void cuandoSubtotalRepuestosEstaEntre50001Y150000_aplicaTreintaYCincoPorCiento() {
    Double manoDeObra = servicioPresupuesto.calcularManoDeObra(100000.0);
    assertThat(manoDeObra, equalTo(35000.0));
  }

  @Test
  public void cuandoSubtotalRepuestosSupera150000SinLlegarAlTope_aplicaVeinticincoPorCiento() {
    Double manoDeObra = servicioPresupuesto.calcularManoDeObra(200000.0);
    assertThat(manoDeObra, equalTo(50000.0));
  }

  @Test
  public void cuandoManoDeObraCalculadaSupera60000_aplicaTopeMaximo() {
    Double manoDeObra = servicioPresupuesto.calcularManoDeObra(400000.0);
    assertThat(manoDeObra, equalTo(60000.0));
  }

  @Test
  public void obtenerRepuestosDisponiblesDeberiaRetornarUnRepuesto() {
    List<Repuesto> repuestosEsperado = new ArrayList<>();
    repuestosEsperado.add(new Repuesto());
    when(repositorioRepuestoMock.obtenerDisponibles()).thenReturn(repuestosEsperado);
    List<Repuesto> repuestosDisponibles = servicioPresupuesto.obtenerRepuestosDisponibles();
    assertThat(repuestosDisponibles.size(), equalTo(1));
  }

  @Test
  public void calcularSubtotalCuandoRepuestoNoexisteDeberiaLanzarIllegalArgumentException() {
    when(repositorioRepuestoMock.buscarPorId(anyLong())).thenReturn(null);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> {
              servicioPresupuesto.calcularSubtotal(99L, 2);
            }
    );

    assertThat(exception.getMessage(), equalTo("El repuesto solicitado no existe."));
  }

  @Test
  public void descontarStockCuandoRepuestoNoExisteDeberiaLanzarIllegalArgumentException() {
    // Cambiado: Ahora valida el mock con bloqueo
    when(repositorioRepuestoMock.buscarPorIdConBloqueo(anyLong())).thenReturn(null);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> {
              servicioPresupuesto.descontarStockRepuesto(99L, 2);
            }
    );

    assertThat(exception.getMessage(), equalTo("El repuesto solicitado no existe."));
  }

  @Test
  public void descontarStockCuandoNoHayStockSuficienteDeberiaLanzarSinStockException() {
    Long repuestoId = 1L;
    Integer cantidadPedida = 5;
    Repuesto repuestoMock = mock(Repuesto.class);

    when(repuestoMock.getStock()).thenReturn(2);
    when(repuestoMock.getNombre()).thenReturn("repuestoMock");

    // Cambiado: Ahora valida el mock con bloqueo
    when(repositorioRepuestoMock.buscarPorIdConBloqueo(anyLong())).thenReturn(repuestoMock);

    SinStockException exception = assertThrows(
            SinStockException.class,
            () -> {
              servicioPresupuesto.descontarStockRepuesto(repuestoId, cantidadPedida);
            }
    );

    assertThat(
            exception.getMessage(),
            equalTo("Stock insuficiente para: " + repuestoMock.getNombre())
    );

    verify(repositorioRepuestoMock, never()).guardar(any());
  }

  @Test
  public void enviarPresupuestoDeberiaActualizarOrdenYGuardarla() throws SinStockException {
    Integer codigoSeguimiento = 12345;
    Double total = 1500.0;

    OrdenReparacion ordenFalsa = new OrdenReparacion();
    List<ItemPresupuestoForm> items = new ArrayList<>();

    when(repositorioOrdenMock.buscarPorCodigo(codigoSeguimiento)).thenReturn(ordenFalsa);

    servicioPresupuesto.enviarPresupuesto(codigoSeguimiento, items, total);

    assertThat(ordenFalsa.getMontoTotal(), equalTo(total));
    assertThat(ordenFalsa.getEstado(), equalTo(EstadoOrden.PRESUPUESTO_ENVIADO));

    verify(repositorioOrdenMock, times(1)).modificarOrden(ordenFalsa);
  }

  @Test
  public void enviarPresupuestoCuandoOrdenNoExisteDeberiaLanzarIllegalArgumentException() {
    Integer codigoSeguimiento = 9999;
    Double total = 1500.0;
    List<ItemPresupuestoForm> items = new ArrayList<>();

    when(repositorioOrdenMock.buscarPorCodigo(codigoSeguimiento)).thenReturn(null);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> {
              servicioPresupuesto.enviarPresupuesto(codigoSeguimiento, items, total);
            }
    );

    assertThat(
            exception.getMessage(),
            equalTo("No se encontró la orden con el código de seguimiento: " + codigoSeguimiento)
    );

    verify(repositorioOrdenMock, never()).modificarOrden(any());
  }

  @Test
  public void calcularSubtotalRepuestosConListaNulaOVaciaDeberiaDevolverCero()
          throws SinStockException {
    Double resultadoNulo = servicioPresupuesto.calcularSubtotalRepuestos(null);
    Double resultadoVacio = servicioPresupuesto.calcularSubtotalRepuestos(new ArrayList<>());

    assertThat(resultadoNulo, equalTo(0.0));
    assertThat(resultadoVacio, equalTo(0.0));
  }

  @Test
  public void calcularSubtotalRepuestosDeberiaIgnorarItemsConDatosInvalidos()
          throws SinStockException {
    List<ItemPresupuestoForm> items = new ArrayList<>();
    items.add(new ItemPresupuestoForm(null, 1));
    items.add(new ItemPresupuestoForm(1L, null));
    items.add(new ItemPresupuestoForm(1L, 0));
    items.add(new ItemPresupuestoForm(1L, -5));

    Double resultado = servicioPresupuesto.calcularSubtotalRepuestos(items);

    assertThat(resultado, equalTo(0.0));
  }

  @Test
  public void calcularSubtotalRepuestosDeberaSumarCorrectamenteItemsValidos()
          throws SinStockException {
    List<ItemPresupuestoForm> items = new ArrayList<>();
    items.add(new ItemPresupuestoForm(1L, 2));

    Repuesto repuestoMock = new Repuesto();
    repuestoMock.setPrecio(1500.0);
    repuestoMock.setStock(5);
    when(repositorioRepuestoMock.buscarPorId(1L)).thenReturn(repuestoMock);

    Double resultado = servicioPresupuesto.calcularSubtotalRepuestos(items);

    assertThat(resultado, equalTo(3000.0));
  }


  @Test
  public void enviarPresupuestoConListaDeRepuestosNulosDeberiaNoFallarYAsignarCalculoTotal()
          throws SinStockException {
    Integer codigo = 12345;
    OrdenReparacion orden = new OrdenReparacion();
    when(repositorioOrdenMock.buscarPorCodigo(codigo)).thenReturn(orden);

    servicioPresupuesto.enviarPresupuesto(codigo, null, 1000.0);

    assertThat(orden.getEstado(), equalTo(EstadoOrden.PRESUPUESTO_ENVIADO));
    assertThat(orden.getMontoTotal(), equalTo(1000.0));
  }

  @Test
  public void enviarPresupuestoDeberiaIgnorarItemsInvalidosYNoDescontarStock()
          throws SinStockException {
    Integer codigo = 12345;
    OrdenReparacion orden = new OrdenReparacion();
    when(repositorioOrdenMock.buscarPorCodigo(codigo)).thenReturn(orden);

    List<ItemPresupuestoForm> items = new ArrayList<>();

    items.add(null);
    items.add(new ItemPresupuestoForm(null, 2));
    items.add(new ItemPresupuestoForm(1L, null));
    items.add(new ItemPresupuestoForm(1L, 0));

    servicioPresupuesto.enviarPresupuesto(codigo, items, 1500.0);

    verify(repositorioRepuestoMock, never()).buscarPorId(anyLong());
    verify(repositorioRepuestoMock, never()).guardar(any(Repuesto.class));
  }

  @Test
  public void enviarPresupuestoConItemsValidosDescuentaElStockCorrectamente()
          throws SinStockException {
    Integer codigo = 12345;
    OrdenReparacion orden = new OrdenReparacion();
    when(repositorioOrdenMock.buscarPorCodigo(codigo)).thenReturn(orden);

    Repuesto repuesto = new Repuesto("RAM", 1000.0, 5);

    // CAMBIADO: Acá se deben simular ambas validaciones
    // La del verificarStock (usa la busqueda normal) y la del descontarStock (usa el bloqueo)
    when(repositorioRepuestoMock.buscarPorId(1L)).thenReturn(repuesto);
    when(repositorioRepuestoMock.buscarPorIdConBloqueo(1L)).thenReturn(repuesto);

    List<ItemPresupuestoForm> items = new ArrayList<>();
    items.add(new ItemPresupuestoForm(1L, 2));

    servicioPresupuesto.enviarPresupuesto(codigo, items, 2500.0);

    assertThat(repuesto.getStock(), equalTo(3));
    verify(repositorioRepuestoMock, times(1)).guardar(repuesto);
  }

  @Test
  public void enviarPresupuestoSiLaOrdenYaEstaEnPresupuestoEnviadoDeberiaLanzarExcepcionYNoDescontarStock() {
    Integer codigo = 12345;
    OrdenReparacion orden = new OrdenReparacion();
    orden.setEstado(EstadoOrden.PRESUPUESTO_ENVIADO);

    when(repositorioOrdenMock.buscarPorCodigo(codigo)).thenReturn(orden);

    List<ItemPresupuestoForm> items = new ArrayList<>();
    items.add(new ItemPresupuestoForm(1L, 2));

    assertThrows(
            IllegalStateException.class,
            () -> {
              servicioPresupuesto.enviarPresupuesto(codigo, items, 2500.0);
            }
    );

    verify(repositorioRepuestoMock, never()).buscarPorId(anyLong());
    verify(repositorioRepuestoMock, never()).guardar(any(Repuesto.class));
    verify(repositorioOrdenMock, never()).modificarOrden(any(OrdenReparacion.class));
  }

  @Test
  public void descontarStockRepuestoDeberiaRestarLaCantidadUsandoBloqueoPesimista() throws SinStockException {
    Long idRepuesto = 1L;
    Integer cantidadADescontar = 2;
    Repuesto repuesto = new Repuesto();
    repuesto.setStock(5);

    when(repositorioRepuestoMock.buscarPorIdConBloqueo(idRepuesto)).thenReturn(repuesto);

    servicioPresupuesto.descontarStockRepuesto(idRepuesto, cantidadADescontar);

    assertThat(repuesto.getStock(), equalTo(3));
    verify(repositorioRepuestoMock, times(1)).buscarPorIdConBloqueo(idRepuesto);
    verify(repositorioRepuestoMock, times(1)).guardar(repuesto);
  }

  @Test
  public void descontarStockRepuestoDeberiaLanzarExcepcionSiNoHayStockSuficienteConBloqueo() {
    Long idRepuesto = 1L;
    Repuesto repuesto = new Repuesto();
    repuesto.setStock(1);

    when(repositorioRepuestoMock.buscarPorIdConBloqueo(idRepuesto)).thenReturn(repuesto);

    assertThrows(
            SinStockException.class,
            () -> servicioPresupuesto.descontarStockRepuesto(idRepuesto, 2)
    );
  }
}