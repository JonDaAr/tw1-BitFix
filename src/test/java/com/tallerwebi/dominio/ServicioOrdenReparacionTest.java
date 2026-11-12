package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DatosCotizacionOnline;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioOrdenReparacionTest {

  private ServicioOrdenReparacion servicioOrdenReparacion;
  private RepositorioUsuario repositorioUsuarioMock;
  private RepositorioOrdenReparacion repositorioOrdenReparacionMock;
  private ServicioAsignacionTecnico servicioAsignacionTecnicoMock;
  private ServicioHistorialOrden servicioHistorialOrdenMock;

  @BeforeEach
  public void init() {
    // 1. Creamos los mocks a la antigua (sin anotaciones)
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.repositorioOrdenReparacionMock = mock(RepositorioOrdenReparacion.class);
    this.servicioAsignacionTecnicoMock = mock(ServicioAsignacionTecnico.class);
    this.servicioHistorialOrdenMock = mock(ServicioHistorialOrden.class);

    // 2. Instanciamos el servicio pasándole los 3 mocks exactos que pide tu constructor
    this.servicioOrdenReparacion =
      new ServicioOrdenReparacionImpl(
        this.repositorioUsuarioMock,
        this.repositorioOrdenReparacionMock,
        this.servicioAsignacionTecnicoMock,
        this.servicioHistorialOrdenMock
      );
  }

  @Test
  public void queSeRegistreUnaOrdenReparacionYSeEnvieAGuardarEnElRepositorio()
    throws DatosIncompletosException, NoHayTecnicosDisponibles {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setNombreCliente("test");
    orden.setTelefonoCliente("test");
    orden.setEmailCliente("test@test.com");
    orden.setModeloEquipo("test");
    orden.setDescripcionFalla("test");
    orden.setNivelReparacion(NivelReparacion.NIVEL_3);

    OrdenReparacion nuevaOrdenReparacion = this.servicioOrdenReparacion.registrarOrden(orden);

    verify(this.servicioAsignacionTecnicoMock, times(1)).asignarTecnicoAOrden(orden);
    verify(this.repositorioOrdenReparacionMock, times(1)).guardarOrden(nuevaOrdenReparacion);
  }

  @Test
  public void queAlRegistrarUnaOrdenReparacionGenereUnCodigoDeSeguimiento()
    throws DatosIncompletosException, NoHayTecnicosDisponibles {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setNombreCliente("test");
    orden.setTelefonoCliente("test");
    orden.setEmailCliente("test@test.com");
    orden.setModeloEquipo("test");
    orden.setDescripcionFalla("test");
    orden.setNivelReparacion(NivelReparacion.NIVEL_3);

    OrdenReparacion nuevaOrdenReparacion = this.servicioOrdenReparacion.registrarOrden(orden);

    assertNotNull(nuevaOrdenReparacion.getCodigoSeguimiento());
  }

  @Test
  public void queNoSePuedaRegistrarUnaOrdenReparacionVaciaYLanceExcepcion() {
    assertThrows(
      DatosIncompletosException.class,
      () -> this.servicioOrdenReparacion.registrarOrden(null)
    );
  }

  @Test
  public void queNoSePuedaRegistrarUnaOrdenReparacionIncompletaYLanceExcepcion() {
    OrdenReparacion datosOrdenMock = mock(OrdenReparacion.class);

    when(datosOrdenMock.getNombreCliente()).thenReturn("test");
    when(datosOrdenMock.getTelefonoCliente()).thenReturn("test");
    when(datosOrdenMock.getModeloEquipo()).thenReturn("");
    when(datosOrdenMock.getDescripcionFalla()).thenReturn("test");

    assertThrows(
      DatosIncompletosException.class,
      () -> this.servicioOrdenReparacion.registrarOrden(datosOrdenMock)
    );
  }

  @Test
  public void queDevuelvaLaOrdenSiElCodigoExiste() throws PedidoNoEncontradoException {
    Integer codigo = 123456;
    OrdenReparacion ordenEsperada = new OrdenReparacion();

    when(repositorioOrdenReparacionMock.buscarPorCodigo(codigo)).thenReturn(ordenEsperada);

    OrdenReparacion ordenObtenida = servicioOrdenReparacion.consultarEstado(codigo);

    assertThat(ordenObtenida, is(notNullValue()));
    verify(repositorioOrdenReparacionMock, times(1)).buscarPorCodigo(codigo);
  }

  @Test
  public void queLanceExcepcionSiElCodigoNoExiste() {
    when(repositorioOrdenReparacionMock.buscarPorCodigo(999999)).thenReturn(null);

    assertThrows(
      PedidoNoEncontradoException.class,
      () -> servicioOrdenReparacion.consultarEstado(999999)
    );
  }

  @Test
  public void queLanceExcepcionSiElCodigoEsNulo() {
    assertThrows(
      PedidoNoEncontradoException.class,
      () -> servicioOrdenReparacion.consultarEstado(null)
    );
  }

  @Test
  public void buscarPorIdDeberiaLlamarABuscarIdOrdenReparacion() {
    Long id = 1L;
    OrdenReparacion ordenEsperadoMock = mock(OrdenReparacion.class);
    when(this.repositorioOrdenReparacionMock.buscarPorId(id)).thenReturn(ordenEsperadoMock);

    OrdenReparacion ordenEncontrado = this.servicioOrdenReparacion.buscarPorId(id);

    assertThat(ordenEncontrado, equalTo(ordenEsperadoMock));
    verify(this.repositorioOrdenReparacionMock, times(1)).buscarPorId(id);
  }

  @Test
  public void listarTodasDeberiaLlamarAListarTodasLasOrdenes() {
    List<OrdenReparacion> listaEsperada = new ArrayList<>();
    when(this.repositorioOrdenReparacionMock.listarTodasLasOrdenes()).thenReturn(listaEsperada);

    List<OrdenReparacion> listaObtenida = this.servicioOrdenReparacion.listarTodas();

    assertThat(listaObtenida, equalTo(listaEsperada));
    verify(this.repositorioOrdenReparacionMock, times(1)).listarTodasLasOrdenes();
  }

  @Test
  public void deberiaActualizarEstadoYNotaTecnicaCorrectamente() {
    EstadoOrden nuevoEstado = EstadoOrden.REPARADO;
    String nuevaNotaTecnica = "Se cambio la pantalla";

    OrdenReparacion orden = new OrdenReparacion();
    orden.setEstado(EstadoOrden.ESPERANDO_REPUESTO);
    orden.setNotaTecnica("en espera de la pantalla");

    when(repositorioOrdenReparacionMock.buscarPorId(anyLong())).thenReturn(orden);

    this.servicioOrdenReparacion.actualizarEstadoYNotaTecnica(
        anyLong(),
        nuevoEstado,
        nuevaNotaTecnica
      );

    assertThat(orden.getEstado(), equalTo(nuevoEstado));
    assertThat(orden.getNotaTecnica(), equalTo(nuevaNotaTecnica));
    verify(repositorioOrdenReparacionMock, times(1)).buscarPorId(anyLong());
  }

  @Test
  public void deberiaRetornarEstadosPermitidosDesdeRecibido() {
    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(EstadoOrden.RECIBIDO);
    assertEquals(2, permitidos.size());
    assertThat(permitidos, containsInAnyOrder(EstadoOrden.RECIBIDO, EstadoOrden.EN_DIAGNOSTICO));
  }

  @Test
  public void deberiaRetornarEstadosPermitidosDesdeEnDiagnostico() {
    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(EstadoOrden.EN_DIAGNOSTICO);
    assertEquals(3, permitidos.size());
    assertThat(
      permitidos,
      containsInAnyOrder(
        EstadoOrden.EN_DIAGNOSTICO,
        EstadoOrden.ESPERANDO_REPUESTO,
        EstadoOrden.REPARADO
      )
    );
  }

  @Test
  public void deberiaRetornarEstadosPermitidosDesdeEsperandoRepuesto() {
    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(EstadoOrden.ESPERANDO_REPUESTO);
    assertEquals(3, permitidos.size());
    assertThat(
      permitidos,
      containsInAnyOrder(
        EstadoOrden.ESPERANDO_REPUESTO,
        EstadoOrden.REPARADO,
        EstadoOrden.EN_REPARACION
      )
    );
  }

  @Test
  public void deberiaRetornarSoloElMismoEstadoSiEsReparado() {
    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(EstadoOrden.REPARADO);
    assertEquals(2, permitidos.size());
    assertThat(permitidos, containsInAnyOrder(EstadoOrden.REPARADO, EstadoOrden.ENTREGADO));
  }

  @Test
  public void deberiaRechazarPresupuestoYCambiarEstado() {
    Integer codigo = 123456;
    String emailCliente = "cliente@mail.com";
    OrdenReparacion orden = new OrdenReparacion();
    orden.setEstado(EstadoOrden.PRESUPUESTO_ENVIADO);
    orden.setEmailCliente(emailCliente);

    when(repositorioOrdenReparacionMock.buscarPorCodigo(codigo)).thenReturn(orden);
    servicioOrdenReparacion.rechazarPresupuesto(codigo, emailCliente);

    assertThat(orden.getEstado(), equalTo(EstadoOrden.PRESUPUESTO_RECHAZADO));
    verify(repositorioOrdenReparacionMock, times(1)).modificarOrden(orden);
  }

  @Test
  public void deberiaLanzarExcepcionAlAceptarPresupuestoDeOtroUsuario() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setEmailCliente("dueño@mail.com");

    when(repositorioOrdenReparacionMock.buscarPorCodigo(123456)).thenReturn(orden);

    assertThrows(
      SecurityException.class,
      () -> servicioOrdenReparacion.aceptarPresupuesto(123456, "intruso@mail.com")
    );
  }

  @Test
  public void queLasOrdenesSeDevuelvanOrdenadasPorPrioridad() {
    OrdenReparacion baja = new OrdenReparacion();
    baja.setFechaIngreso(LocalDateTime.now().minusDays(1));

    OrdenReparacion alta = new OrdenReparacion();
    alta.setFechaIngreso(LocalDateTime.now().minusDays(10));

    OrdenReparacion media = new OrdenReparacion();
    media.setFechaIngreso(LocalDateTime.now().minusDays(5));

    when(this.repositorioOrdenReparacionMock.buscarTodas()).thenReturn(List.of(baja, alta, media));

    List<OrdenReparacion> resultado = this.servicioOrdenReparacion.obtenerOrdenesParaTecnico();

    assertThat(resultado.get(0).getPrioridad(), is(Prioridad.ALTA));
  }

  @Test
  public void queDentroDeLaMismaPrioridadLaMasViejaVayaPrimero() {
    OrdenReparacion vieja = new OrdenReparacion();
    vieja.setFechaIngreso(LocalDateTime.now().minusDays(10));

    OrdenReparacion menosVieja = new OrdenReparacion();
    menosVieja.setFechaIngreso(LocalDateTime.now().minusDays(8));

    when(this.repositorioOrdenReparacionMock.buscarTodas()).thenReturn(List.of(menosVieja, vieja));

    List<OrdenReparacion> resultado = this.servicioOrdenReparacion.obtenerOrdenesParaTecnico();

    assertThat(resultado.get(0).getFechaIngreso(), is(vieja.getFechaIngreso()));
    assertThat(resultado.get(1).getFechaIngreso(), is(menosVieja.getFechaIngreso()));
  }

  @Test
  public void queLasEntregadasVayanAlFinalOrdenadasPorFechaEntrega() {
    OrdenReparacion entregadaVieja = new OrdenReparacion();
    entregadaVieja.setEstado(EstadoOrden.ENTREGADO);
    entregadaVieja.setFechaIngreso(LocalDateTime.now().minusDays(10));
    entregadaVieja.setFechaEntrega(LocalDateTime.now().minusDays(3));

    OrdenReparacion activa = new OrdenReparacion();
    activa.setFechaIngreso(LocalDateTime.now().minusDays(1));

    OrdenReparacion entregadaReciente = new OrdenReparacion();
    entregadaReciente.setEstado(EstadoOrden.ENTREGADO);
    entregadaReciente.setFechaIngreso(LocalDateTime.now().minusDays(5));
    entregadaReciente.setFechaEntrega(LocalDateTime.now().minusDays(1));

    when(this.repositorioOrdenReparacionMock.buscarTodas())
      .thenReturn(List.of(entregadaVieja, entregadaReciente, activa));

    List<OrdenReparacion> resultado = this.servicioOrdenReparacion.obtenerOrdenesParaTecnico();

    assertThat(resultado.get(0).getEstado(), is(not(EstadoOrden.ENTREGADO)));
    assertThat(resultado.get(1).getFechaEntrega(), is(entregadaVieja.getFechaEntrega()));
    assertThat(resultado.get(2).getFechaEntrega(), is(entregadaReciente.getFechaEntrega()));
  }

  @Test
  public void queEntregadaSinFechaEntregaLanceExcepcion() {
    OrdenReparacion entregadaSinFecha = new OrdenReparacion();
    entregadaSinFecha.setEstado(EstadoOrden.ENTREGADO);
    entregadaSinFecha.setFechaIngreso(LocalDateTime.now().minusDays(10));

    OrdenReparacion otraEntregada = new OrdenReparacion();
    otraEntregada.setEstado(EstadoOrden.ENTREGADO);
    otraEntregada.setFechaEntrega(LocalDateTime.now().minusDays(1));

    when(this.repositorioOrdenReparacionMock.buscarTodas())
      .thenReturn(List.of(entregadaSinFecha, otraEntregada));

    assertThrows(
      com.tallerwebi.dominio.excepcion.FechaEntregaNoDefinidaException.class,
      () -> this.servicioOrdenReparacion.obtenerOrdenesParaTecnico()
    );
  }

  @Test
  public void obtenerOrdenesDelClienteDeberiaBuscarPorEmailDelCliente() {
    List<OrdenReparacion> ordenesEsperadas = List.of(new OrdenReparacion(), new OrdenReparacion());
    when(repositorioOrdenReparacionMock.buscarPorEmailCliente("cliente@test.com"))
      .thenReturn(ordenesEsperadas);

    List<OrdenReparacion> ordenesObtenidas = servicioOrdenReparacion.obtenerOrdenesDelCliente(
      "cliente@test.com"
    );

    assertThat(ordenesObtenidas, equalTo(ordenesEsperadas));
    verify(repositorioOrdenReparacionMock, times(1)).buscarPorEmailCliente("cliente@test.com");
  }

  @Test
  public void obtenerOrdenesDelTecnicoDeberiaBuscarPorIdDelTecnico() {
    List<OrdenReparacion> ordenesEsperadas = List.of(new OrdenReparacion(), new OrdenReparacion());
    when(repositorioOrdenReparacionMock.buscarPorTecnico(10L)).thenReturn(ordenesEsperadas);

    List<OrdenReparacion> ordenesObtenidas = servicioOrdenReparacion.obtenerOrdenesDelTecnico(10L);

    assertThat(ordenesObtenidas, equalTo(ordenesEsperadas));
    verify(repositorioOrdenReparacionMock, times(1)).buscarPorTecnico(10L);
  }

  @Test
  public void obtenerHistorialDelTecnicoDeberiaDevolverUnaListaDeOrdenesSegunElIdDelTecnico() {
    List<OrdenReparacion> historial = servicioOrdenReparacion.obtenerHistorialDelTecnico(1L);
    assertNotNull(historial);
  }

  @Test
  public void obtenerHistorialDelTecnicoDeberiaDevolverDatosCorrespondientesDesdeElRepositorio() {
    List<OrdenReparacion> listaMock = new ArrayList<>();
    OrdenReparacion nuevaOrdenMock = mock(OrdenReparacion.class);
    listaMock.add(nuevaOrdenMock);

    when(repositorioOrdenReparacionMock.buscarOrdenesCompletadasPorTecnico(1L))
      .thenReturn(listaMock);

    List<OrdenReparacion> historial = servicioOrdenReparacion.obtenerHistorialDelTecnico(1L);

    assertEquals(1, historial.size());
    assertEquals(nuevaOrdenMock, historial.get(0));
  }

  @Test
  public void obtenerEstadosPermitidosParaPresupuestoAceptadoDeberiaDevolverEnReparacionYEsperandoRepuesto() {
    List<EstadoOrden> permitidos = servicioOrdenReparacion.obtenerEstadosPermitidosPara(
      EstadoOrden.PRESUPUESTO_ACEPTADO
    );
    assertThat(
      permitidos,
      hasItems(
        EstadoOrden.PRESUPUESTO_ACEPTADO,
        EstadoOrden.EN_REPARACION,
        EstadoOrden.ESPERANDO_REPUESTO
      )
    );
    assertThat(permitidos.size(), equalTo(3));
  }

  @Test
  public void obtenerEstadosPermitidosParaEnReparacionDeberiaDevolverReparadoYEsperandoRepuesto() {
    List<EstadoOrden> permitidos = servicioOrdenReparacion.obtenerEstadosPermitidosPara(
      EstadoOrden.EN_REPARACION
    );
    assertThat(
      permitidos,
      hasItems(EstadoOrden.EN_REPARACION, EstadoOrden.REPARADO, EstadoOrden.ESPERANDO_REPUESTO)
    );
    assertThat(permitidos.size(), equalTo(3));
  }

  @Test
  public void obtenerEstadosPermitidosParaEsperandoRepuestoDeberiaPermitirVolverAEnReparacionOReparado() {
    List<EstadoOrden> permitidos = servicioOrdenReparacion.obtenerEstadosPermitidosPara(
      EstadoOrden.ESPERANDO_REPUESTO
    );
    assertThat(
      permitidos,
      hasItems(EstadoOrden.ESPERANDO_REPUESTO, EstadoOrden.REPARADO, EstadoOrden.EN_REPARACION)
    );
    assertThat(permitidos.size(), equalTo(3));
  }

  @Test
  public void deberiaRetornarCorrectamenteEstadosPermitidosParaPendienteTriaje() {
    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(EstadoOrden.PENDIENTE_TRIAJE);
    assertEquals(2, permitidos.size());
    assertThat(
      permitidos,
      containsInAnyOrder(EstadoOrden.PENDIENTE_TRIAJE, EstadoOrden.PENDIENTE_PRESUPUESTO)
    );
  }

  @Test
  public void deberiaRetornarCorrectamenteEstadosPermitidosParaPendientePresupuesto() {
    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(EstadoOrden.PENDIENTE_PRESUPUESTO);
    assertEquals(2, permitidos.size());
    assertThat(
      permitidos,
      containsInAnyOrder(EstadoOrden.PENDIENTE_PRESUPUESTO, EstadoOrden.PRESUPUESTO_ENVIADO)
    );
  }

  @Test
  public void deberiaRetornarCorrectamenteEstadosPermitidosParaEsperandoIngreso() {
    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(EstadoOrden.ESPERANDO_INGRESO);
    assertEquals(2, permitidos.size());
    assertThat(permitidos, containsInAnyOrder(EstadoOrden.ESPERANDO_INGRESO, EstadoOrden.RECIBIDO));
  }

  @Test
  public void aceptarPresupuestoCuandoOrdenNoExisteDeberiaLanzarExcepcion() {
    when(repositorioOrdenReparacionMock.buscarPorCodigo(9999)).thenReturn(null);
    assertThrows(
      IllegalArgumentException.class,
      () -> servicioOrdenReparacion.aceptarPresupuesto(9999, "cliente@mail.com")
    );
  }

  @Test
  public void aceptarPresupuestoCuandoEstadoNoEsPresupuestoEnviadoDeberaLanzarExcepcion() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setEmailCliente("cliente@mail.com");
    orden.setEstado(EstadoOrden.RECIBIDO);

    when(repositorioOrdenReparacionMock.buscarPorCodigo(12345)).thenReturn(orden);
    assertThrows(
      IllegalStateException.class,
      () -> servicioOrdenReparacion.aceptarPresupuesto(12345, "cliente@mail.com")
    );
  }

  @Test
  public void rechazarPresupuestoCuandoOrdenNoExisteDeberiaLanzarExcepcion() {
    when(repositorioOrdenReparacionMock.buscarPorCodigo(9999)).thenReturn(null);
    assertThrows(
      IllegalArgumentException.class,
      () -> servicioOrdenReparacion.rechazarPresupuesto(9999, "cliente@mail.com")
    );
  }

  @Test
  public void rechazarPresupuestoCuandoEstadoNoEsPresupuestoEnviadoDeberiaLanzaExcepcion() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setEmailCliente("cliente@mail.com");
    orden.setEstado(EstadoOrden.EN_DIAGNOSTICO);

    when(repositorioOrdenReparacionMock.buscarPorCodigo(12345)).thenReturn(orden);
    assertThrows(
      IllegalStateException.class,
      () -> servicioOrdenReparacion.rechazarPresupuesto(12345, "cliente@mail.com")
    );
  }

  @Test
  public void deberiaLanzarExcepcionSiNoHayTecnicosConElNivelRequeridoParaLaOrden()
    throws NoHayTecnicosDisponibles {
    OrdenReparacion orden = new OrdenReparacion("María", "654321", "Servidor", "Falla placa base");
    orden.setEmailCliente("maria@test.com");
    orden.setNivelReparacion(NivelReparacion.NIVEL_3);

    doThrow(NoHayTecnicosDisponibles.class)
      .when(servicioAsignacionTecnicoMock)
      .asignarTecnicoAOrden(orden);

    assertThrows(
      NoHayTecnicosDisponibles.class,
      () -> this.servicioOrdenReparacion.registrarOrden(orden)
    );
  }

  @Test
  public void registrarCotizacionOnlineParaTriajeDeberiaGuardarEnPendienteTriajeSinAsignarTecnico()
    throws DatosIncompletosException {
    DatosCotizacionOnline datos = new DatosCotizacionOnline();
    datos.setNombreCliente("Santiago");
    datos.setEmailCliente("santiago@mail.com");
    datos.setModeloEquipo("Lenovo ThinkPad");
    datos.setDescripcionFalla("No enciende");

    OrdenReparacion ordenResult = servicioOrdenReparacion.registrarCotizacionOnlineParaTriaje(
      datos
    );

    assertThat(ordenResult.getEstado(), equalTo(EstadoOrden.PENDIENTE_TRIAJE));
    assertNull(ordenResult.getTecnicoAsignado());
    assertNotNull(ordenResult.getCodigoSeguimiento());
    verify(repositorioOrdenReparacionMock, times(1))
      .guardarOrden(org.mockito.ArgumentMatchers.any(OrdenReparacion.class));
  }

  @Test
  public void registrarCotizacionOnlineParaTriajeConDatosIncompletosDeberiaLanzarExcepcion() {
    assertThrows(
      DatosIncompletosException.class,
      () -> servicioOrdenReparacion.registrarCotizacionOnlineParaTriaje(new DatosCotizacionOnline())
    );
  }

  @Test
  public void queAlDerivarUnaCotizacionSeAsigneNivelCambieEstadoYSeGuardeEnBD()
    throws NoHayTecnicosDisponibles {
    Long idOrden = 1L;
    OrdenReparacion ordenMock = new OrdenReparacion();
    ordenMock.setIdOrdenReparacion(idOrden);
    ordenMock.setEstado(EstadoOrden.PENDIENTE_TRIAJE);

    when(repositorioOrdenReparacionMock.buscarPorId(idOrden)).thenReturn(ordenMock);

    servicioOrdenReparacion.derivarCotizacion(idOrden, NivelReparacion.NIVEL_1);

    assertEquals(NivelReparacion.NIVEL_1, ordenMock.getNivelReparacion());
    assertEquals(EstadoOrden.PENDIENTE_PRESUPUESTO, ordenMock.getEstado());

    verify(servicioAsignacionTecnicoMock, times(1)).asignarTecnicoAOrden(ordenMock);
    verify(repositorioOrdenReparacionMock, times(1)).modificarOrden(ordenMock);
  }

  @Test
  public void queAlDerivarUnaCotizacionInexistenteLanceExcepcion() {
    when(repositorioOrdenReparacionMock.buscarPorId(99L)).thenReturn(null);

    assertThrows(
      IllegalArgumentException.class,
      () -> servicioOrdenReparacion.derivarCotizacion(99L, NivelReparacion.NIVEL_1)
    );
  }

  @Test
  public void queRegistreEventoSiCambiaElEstado() {
    OrdenReparacion orden = new OrdenReparacion(); // arranca en RECIBIDO
    when(repositorioOrdenReparacionMock.buscarPorId(1L)).thenReturn(orden);

    servicioOrdenReparacion.actualizarEstadoYNotaTecnica(1L, EstadoOrden.EN_DIAGNOSTICO, "nota");

    verify(servicioHistorialOrdenMock).registrarEvento(orden, EstadoOrden.EN_DIAGNOSTICO);
  }

  @Test
  public void queNoRegistreEventoSiElEstadoNoCambia() {
    OrdenReparacion orden = new OrdenReparacion(); // RECIBIDO
    when(repositorioOrdenReparacionMock.buscarPorId(1L)).thenReturn(orden);

    servicioOrdenReparacion.actualizarEstadoYNotaTecnica(1L, EstadoOrden.RECIBIDO, "solo nota");

    verify(servicioHistorialOrdenMock, never()).registrarEvento(any(), any());
  }

  @Test
  public void queSePudeReasignarUnaOrdenCorrectamente() {
    // Preparación
    Long idOrden = 1L;
    Long idNuevoTecnico = 2L;

    OrdenReparacion ordenMock = mock(OrdenReparacion.class);
    Usuario nuevoTecnicoMock = mock(Usuario.class);

    when(repositorioOrdenReparacionMock.buscarPorId(idOrden)).thenReturn(ordenMock);
    when(repositorioUsuarioMock.buscarPorId(idNuevoTecnico)).thenReturn(nuevoTecnicoMock);

    // Ejecución
    servicioOrdenReparacion.reasignarOrden(idOrden, idNuevoTecnico);

    // Validación
    verify(ordenMock, times(1)).setTecnicoAsignado(nuevoTecnicoMock);
    verify(ordenMock, times(1)).setFechaAsignacion(any());
    verify(repositorioOrdenReparacionMock, times(1)).modificarOrden(ordenMock);
  }

  @Test
  public void queLanceExcepcionSiLaOrdenOElTecnicoNoExistenAlReasignar() {
    // Preparación (orden null, técnico válido)
    when(repositorioOrdenReparacionMock.buscarPorId(1L)).thenReturn(null);
    when(repositorioUsuarioMock.buscarPorId(2L)).thenReturn(mock(Usuario.class));

    // Ejecución y Validación
    assertThrows(IllegalArgumentException.class, () -> {
      servicioOrdenReparacion.reasignarOrden(1L, 2L);
    });

    verify(repositorioOrdenReparacionMock, never()).modificarOrden(any());
  }

  @Test
  public void queSePuedaReasignarUnaOrdenCorrectamente() {
    // Preparación
    Long idOrden = 1L;
    Long idNuevoTecnico = 2L;

    OrdenReparacion ordenMock = mock(OrdenReparacion.class);
    Usuario nuevoTecnicoMock = mock(Usuario.class);

    when(repositorioOrdenReparacionMock.buscarPorId(idOrden)).thenReturn(ordenMock);
    when(repositorioUsuarioMock.buscarPorId(idNuevoTecnico)).thenReturn(nuevoTecnicoMock);

    // Ejecución
    servicioOrdenReparacion.reasignarOrden(idOrden, idNuevoTecnico);

    // Validación
    verify(ordenMock, times(1)).setTecnicoAsignado(nuevoTecnicoMock);
    verify(ordenMock, times(1)).setFechaAsignacion(any());
    verify(repositorioOrdenReparacionMock, times(1)).modificarOrden(ordenMock);
  }
}
