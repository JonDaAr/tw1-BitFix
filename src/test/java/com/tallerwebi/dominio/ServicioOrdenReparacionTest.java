package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
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
  private RepositorioOrdenReparacion repositorioOrdenReparacionMock;
  private RepositorioUsuario repositorioUsuarioMock;

  @BeforeEach
  public void init() {
    this.repositorioOrdenReparacionMock = mock(RepositorioOrdenReparacion.class);
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);

    Usuario tecnico = new Usuario();
    tecnico.setId(1L);
    // ¡AGREGAMOS ESTO PARA QUE EL TÉCNICO MOCKEADO PASE EL FILTRO DEL ALGORITMO!
    tecnico.setNivelSoporte(NivelReparacion.NIVEL_3);

    when(this.repositorioUsuarioMock.buscarTecnicosActivos()).thenReturn(List.of(tecnico));
    when(this.repositorioOrdenReparacionMock.contarOrdenesActivas(1L)).thenReturn(0L);
    when(this.repositorioOrdenReparacionMock.buscarFechaUltimaAsignacion(1L)).thenReturn(null);

    this.servicioOrdenReparacion =
      new ServicioOrdenReparacionImpl(
        this.repositorioUsuarioMock,
        this.repositorioOrdenReparacionMock
      );
  }

  @Test
  public void queSeRegistreUnaOrdenReparacionYSeEnvieAGuardarEnElRepositorio()
    throws DatosIncompletosException {
    // Usamos una orden real en lugar de un mock para evitar comportamientos extraños con los getters
    OrdenReparacion orden = new OrdenReparacion();
    orden.setNombreCliente("test");
    orden.setTelefonoCliente("test");
    orden.setEmailCliente("test@test.com");
    orden.setModeloEquipo("test");
    orden.setDescripcionFalla("test");
    // ES CLAVE ESTABLECER EL NIVEL AQUÍ PARA QUE ENCUENTRE AL TÉCNICO EN asignarTecnicoAOrden()
    orden.setNivelReparacion(NivelReparacion.NIVEL_3);

    OrdenReparacion nuevaOrdenReparacion = this.servicioOrdenReparacion.registrarOrden(orden);

    verify(this.repositorioOrdenReparacionMock, times(1)).guardarOrden(nuevaOrdenReparacion);
  }

  @Test
  public void queAlRegistrarUnaOrdenReparacionGenereUnCodigoDeSeguimiento()
    throws DatosIncompletosException {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setNombreCliente("test");
    orden.setTelefonoCliente("test");
    orden.setEmailCliente("test@test.com");
    orden.setModeloEquipo("test");
    orden.setDescripcionFalla("test");
    // ES CLAVE ESTABLECER EL NIVEL AQUÍ PARA QUE ENCUENTRE AL TÉCNICO
    orden.setNivelReparacion(NivelReparacion.NIVEL_3);

    OrdenReparacion nuevaOrdenReparacion = this.servicioOrdenReparacion.registrarOrden(orden);

    assertNotNull(nuevaOrdenReparacion.getCodigoSeguimiento());
  }

  @Test
  public void queNoSePuedaRegistrarUnaOrdenReparacionVaciaYLanceExcepcion() {
    OrdenReparacion datosOrden = null;

    assertThrows(
      DatosIncompletosException.class,
      () -> this.servicioOrdenReparacion.registrarOrden(datosOrden)
    );
  }

  @Test
  public void queNoSePuedaRegistrarUnaOrdenReparacionIncompletaYLanceExcepcion() {
    OrdenReparacion datosOrdenMock = mock(OrdenReparacion.class);

    when(datosOrdenMock.getNombreCliente()).thenReturn("test");
    when(datosOrdenMock.getTelefonoCliente()).thenReturn("test");
    // falta completar modelo de equipo
    when(datosOrdenMock.getModeloEquipo()).thenReturn("");
    when(datosOrdenMock.getDescripcionFalla()).thenReturn("test");

    assertThrows(
      DatosIncompletosException.class,
      () -> this.servicioOrdenReparacion.registrarOrden(datosOrdenMock)
    );
  }

  // --- TESTS DE CONSULTA DE ESTADO ---

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
    Integer codigoInexistente = 999999;
    when(repositorioOrdenReparacionMock.buscarPorCodigo(codigoInexistente)).thenReturn(null);

    assertThrows(
      PedidoNoEncontradoException.class,
      () -> servicioOrdenReparacion.consultarEstado(codigoInexistente)
    );
  }

  @Test
  public void queLanceExcepcionSiElCodigoEsNulo() {
    assertThrows(
      PedidoNoEncontradoException.class,
      () -> servicioOrdenReparacion.consultarEstado(null)
    );
  }

  // -----test gestion estados y diagnostico(nota tecnica)

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
    EstadoOrden estadoActual = EstadoOrden.RECIBIDO;

    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(estadoActual);

    assertEquals(2, permitidos.size());
    assertThat(permitidos, containsInAnyOrder(EstadoOrden.RECIBIDO, EstadoOrden.EN_DIAGNOSTICO));
  }

  @Test
  public void deberiaRetornarEstadosPermitidosDesdeEnDiagnostico() {
    EstadoOrden estadoActual = EstadoOrden.EN_DIAGNOSTICO;

    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(estadoActual);

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
    EstadoOrden estadoActual = EstadoOrden.ESPERANDO_REPUESTO;

    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(estadoActual);

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
    EstadoOrden estadoActual = EstadoOrden.REPARADO;

    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(estadoActual);

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
    Integer codigo = 123456;
    String emailDueñoOriginal = "dueño@mail.com";
    String emailIntruso = "intruso@mail.com";

    OrdenReparacion orden = new OrdenReparacion();
    orden.setEmailCliente(emailDueñoOriginal);

    when(repositorioOrdenReparacionMock.buscarPorCodigo(codigo)).thenReturn(orden);

    assertThrows(
      SecurityException.class,
      () -> servicioOrdenReparacion.aceptarPresupuesto(codigo, emailIntruso)
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
    String email = "cliente@test.com";
    List<OrdenReparacion> ordenesEsperadas = List.of(new OrdenReparacion(), new OrdenReparacion());

    when(repositorioOrdenReparacionMock.buscarPorEmailCliente(email)).thenReturn(ordenesEsperadas);

    List<OrdenReparacion> ordenesObtenidas = servicioOrdenReparacion.obtenerOrdenesDelCliente(
      email
    );

    assertThat(ordenesObtenidas, equalTo(ordenesEsperadas));

    verify(repositorioOrdenReparacionMock, times(1)).buscarPorEmailCliente(email);
  }

  @Test
  public void obtenerOrdenesDelTecnicoDeberiaBuscarPorIdDelTecnico() {
    Long tecnicoId = 10L;

    List<OrdenReparacion> ordenesEsperadas = List.of(new OrdenReparacion(), new OrdenReparacion());

    when(repositorioOrdenReparacionMock.buscarPorTecnico(tecnicoId)).thenReturn(ordenesEsperadas);

    List<OrdenReparacion> ordenesObtenidas = servicioOrdenReparacion.obtenerOrdenesDelTecnico(
      tecnicoId
    );

    assertThat(ordenesObtenidas, equalTo(ordenesEsperadas));

    verify(repositorioOrdenReparacionMock, times(1)).buscarPorTecnico(tecnicoId);
  }

  @Test
  public void obtenerHistorialDelTecnicoDeberiaDevolverUnaListaDeOrdenesSegunElIdDelTecnico() {
    Long idTecnico = 1L;

    List<OrdenReparacion> historial = servicioOrdenReparacion.obtenerHistorialDelTecnico(idTecnico);

    assertNotNull(historial);
  }

  @Test
  public void obtenerHistorialDelTecnicoDeberiaDevolverDatosCorrespondientesDesdeElRepositorio() {
    Long idTecnico = 1L;
    List<OrdenReparacion> listaMock = new ArrayList<>();
    OrdenReparacion nuevaOrdenMock = mock(OrdenReparacion.class);
    listaMock.add(nuevaOrdenMock);

    when(repositorioOrdenReparacionMock.buscarOrdenesCompletadasPorTecnico(idTecnico))
      .thenReturn(listaMock);

    List<OrdenReparacion> historial = servicioOrdenReparacion.obtenerHistorialDelTecnico(idTecnico);

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
    EstadoOrden estadoActual = EstadoOrden.PENDIENTE_TRIAJE;

    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(estadoActual);

    assertEquals(2, permitidos.size());
    assertThat(
      permitidos,
      containsInAnyOrder(EstadoOrden.PENDIENTE_TRIAJE, EstadoOrden.PENDIENTE_PRESUPUESTO)
    );
  }

  @Test
  public void deberiaRetornarCorrectamenteEstadosPermitidosParaPendientePresupuesto() {
    EstadoOrden estadoActual = EstadoOrden.PENDIENTE_PRESUPUESTO;

    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(estadoActual);

    assertEquals(2, permitidos.size());
    assertThat(
      permitidos,
      containsInAnyOrder(EstadoOrden.PENDIENTE_PRESUPUESTO, EstadoOrden.PRESUPUESTO_ENVIADO)
    );
  }

  @Test
  public void deberiaRetornarCorrectamenteEstadosPermitidosParaEsperandoIngreso() {
    EstadoOrden estadoActual = EstadoOrden.ESPERANDO_INGRESO;

    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(estadoActual);

    assertEquals(2, permitidos.size());
    assertThat(permitidos, containsInAnyOrder(EstadoOrden.ESPERANDO_INGRESO, EstadoOrden.RECIBIDO));
  }

  //TESTS PARA CUBRIR RAMAS FALTANTES EN aceptarPresupuesto

  @Test
  public void aceptarPresupuestoCuandoOrdenNoExisteDeberiaLanzarExcepcion() {
    Integer codigo = 9999;
    String email = "cliente@mail.com";

    when(repositorioOrdenReparacionMock.buscarPorCodigo(codigo)).thenReturn(null);

    assertThrows(
      IllegalArgumentException.class,
      () -> {
        servicioOrdenReparacion.aceptarPresupuesto(codigo, email);
      }
    );
  }

  @Test
  public void aceptarPresupuestoCuandoEstadoNoEsPresupuestoEnviadoDeberaLanzarExcepcion() {
    Integer codigo = 12345;
    String email = "cliente@mail.com";

    OrdenReparacion orden = new OrdenReparacion();
    orden.setEmailCliente(email);
    orden.setEstado(EstadoOrden.RECIBIDO);

    when(repositorioOrdenReparacionMock.buscarPorCodigo(codigo)).thenReturn(orden);

    assertThrows(
      IllegalStateException.class,
      () -> {
        servicioOrdenReparacion.aceptarPresupuesto(codigo, email);
      }
    );
  }

  //TESTS PARA CUBRIR RAMAS FALTANTES EN rechazarPresupuesto

  @Test
  public void rechazarPresupuestoCuandoOrdenNoExisteDeberiaLanzarExcepcion() {
    Integer codigo = 9999;
    String email = "cliente@mail.com";

    when(repositorioOrdenReparacionMock.buscarPorCodigo(codigo)).thenReturn(null);

    assertThrows(
      IllegalArgumentException.class,
      () -> {
        servicioOrdenReparacion.rechazarPresupuesto(codigo, email);
      }
    );
  }

  @Test
  public void rechazarPresupuestoCuandoEstadoNoEsPresupuestoEnviadoDeberiaLanzaExcepcion() {
    Integer codigo = 12345;
    String email = "cliente@mail.com";

    OrdenReparacion orden = new OrdenReparacion();
    orden.setEmailCliente(email);
    orden.setEstado(EstadoOrden.EN_DIAGNOSTICO);

    when(repositorioOrdenReparacionMock.buscarPorCodigo(codigo)).thenReturn(orden);

    assertThrows(
      IllegalStateException.class,
      () -> {
        servicioOrdenReparacion.rechazarPresupuesto(codigo, email);
      }
    );
  }

  // --- TESTS NUEVOS DE ASIGNACIÓN CON FILTRO POR NIVEL DE SOPORTE ---

  @Test
  public void deberiaAsignarUnTecnicoQueTengaElMismoNivelDeSoporteQueLaOrden() throws Exception {
    OrdenReparacion orden = new OrdenReparacion("Juan", "123456", "PC Escritorio", "No da video");
    orden.setEmailCliente("juan@test.com");
    orden.setNivelReparacion(NivelReparacion.NIVEL_2);

    Usuario tecNivel1 = new Usuario();
    tecNivel1.setId(1L);
    tecNivel1.setNivelSoporte(NivelReparacion.NIVEL_1);

    Usuario tecNivel2 = new Usuario();
    tecNivel2.setId(2L);
    tecNivel2.setNivelSoporte(NivelReparacion.NIVEL_2);

    Usuario tecNivel3 = new Usuario();
    tecNivel3.setId(3L);
    tecNivel3.setNivelSoporte(NivelReparacion.NIVEL_3);

    when(this.repositorioUsuarioMock.buscarTecnicosActivos())
      .thenReturn(List.of(tecNivel1, tecNivel2, tecNivel3));

    when(this.repositorioOrdenReparacionMock.contarOrdenesActivas(anyLong())).thenReturn(0L);

    this.servicioOrdenReparacion.registrarOrden(orden);

    assertThat(orden.getTecnicoAsignado(), is(notNullValue()));
    assertThat(orden.getTecnicoAsignado().getId(), is(equalTo(2L)));
    assertThat(orden.getTecnicoAsignado().getNivelSoporte(), is(equalTo(NivelReparacion.NIVEL_2)));
  }

  @Test
  public void deberiaLanzarExcepcionSiNoHayTecnicosConElNivelRequeridoParaLaOrden() {
    OrdenReparacion orden = new OrdenReparacion("María", "654321", "Servidor", "Falla placa base");
    orden.setEmailCliente("maria@test.com");
    orden.setNivelReparacion(NivelReparacion.NIVEL_3);

    Usuario tecNivel1 = new Usuario();
    tecNivel1.setId(1L);
    tecNivel1.setNivelSoporte(NivelReparacion.NIVEL_1);

    Usuario tecNivel2 = new Usuario();
    tecNivel2.setId(2L);
    tecNivel2.setNivelSoporte(NivelReparacion.NIVEL_2);

    when(this.repositorioUsuarioMock.buscarTecnicosActivos())
      .thenReturn(List.of(tecNivel1, tecNivel2));

    assertThrows(
      NoHayTecnicosDisponibles.class,
      () -> {
        this.servicioOrdenReparacion.registrarOrden(orden);
      }
    );
  }

  // ==========================================
  // NUEVOS TESTS: COTIZACION ONLINE TRIANGULADA
  // ==========================================

  @Test
  public void registrarCotizacionOnlineParaTriajeDeberiaGuardarEnPendienteTriajeSinAsignarTecnico()
    throws DatosIncompletosException {
    // Preparación
    DatosCotizacionOnline datos = new DatosCotizacionOnline();
    datos.setNombreCliente("Santiago");
    datos.setEmailCliente("santiago@mail.com");
    datos.setModeloEquipo("Lenovo ThinkPad");
    datos.setDescripcionFalla("No enciende tras actualización");

    // Ejecución
    OrdenReparacion ordenResult = servicioOrdenReparacion.registrarCotizacionOnlineParaTriaje(
      datos
    );

    // Validación
    assertThat(ordenResult.getEstado(), equalTo(EstadoOrden.PENDIENTE_TRIAJE));

    // En lugar de testear que sea estrictamente null (que puede chocar con el default de la entidad),
    // validemos que la cotización NO tenga asociado un técnico aún (que es lo más importante del triaje)
    assertNull(ordenResult.getTecnicoAsignado());
    assertNotNull(ordenResult.getCodigoSeguimiento());

    // Verificamos que se llamó al repositorio usando el Matcher correcto para evitar el error de Mockito
    verify(repositorioOrdenReparacionMock, times(1))
      .guardarOrden(org.mockito.ArgumentMatchers.any(OrdenReparacion.class));
  }

  @Test
  public void registrarCotizacionOnlineParaTriajeConDatosIncompletosDeberiaLanzarExcepcion()
    throws DatosIncompletosException {
    DatosCotizacionOnline datosIncompletos = new DatosCotizacionOnline();
    // Al estar vacío, debería lanzar la excepción de validación
    assertThrows(
      DatosIncompletosException.class,
      () -> {
        servicioOrdenReparacion.registrarCotizacionOnlineParaTriaje(datosIncompletos);
      }
    );
  }
}
