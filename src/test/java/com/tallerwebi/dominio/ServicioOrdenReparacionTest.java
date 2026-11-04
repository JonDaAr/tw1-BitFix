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
    OrdenReparacion datosOrdenMock = mock(OrdenReparacion.class);

    when(datosOrdenMock.getNombreCliente()).thenReturn("test");
    when(datosOrdenMock.getTelefonoCliente()).thenReturn("test");
    when(datosOrdenMock.getEmailCliente()).thenReturn("test@test.com");
    when(datosOrdenMock.getModeloEquipo()).thenReturn("test");
    when(datosOrdenMock.getDescripcionFalla()).thenReturn("test");

    OrdenReparacion nuevaOrdenReparacion =
      this.servicioOrdenReparacion.registrarOrden(datosOrdenMock);

    verify(this.repositorioOrdenReparacionMock, times(1)).guardarOrden(nuevaOrdenReparacion);
  }

  @Test
  public void queAlRegistrarUnaOrdenReparacionGenereUnCodigoDeSeguimiento()
    throws DatosIncompletosException {
    OrdenReparacion datosOrdenMock = mock(OrdenReparacion.class);

    when(datosOrdenMock.getNombreCliente()).thenReturn("test");
    when(datosOrdenMock.getTelefonoCliente()).thenReturn("test");
    when(datosOrdenMock.getEmailCliente()).thenReturn("test@test.com");
    when(datosOrdenMock.getModeloEquipo()).thenReturn("test");
    when(datosOrdenMock.getDescripcionFalla()).thenReturn("test");

    OrdenReparacion nuevaOrdenReparacion =
      this.servicioOrdenReparacion.registrarOrden(datosOrdenMock);

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

    // Esperamos 3 estados permitidos
    assertEquals(3, permitidos.size());
    assertThat(
      permitidos,
      containsInAnyOrder(
        EstadoOrden.ESPERANDO_REPUESTO,
        EstadoOrden.REPARADO,
        EstadoOrden.EN_REPARACION // <-- Agregamos este
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
    orden.setEmailCliente(emailCliente); // Simulamos que la orden es del cliente

    when(repositorioOrdenReparacionMock.buscarPorCodigo(codigo)).thenReturn(orden);

    // Llamamos al método pasando el código y el email para pasar la validación IDOR
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

    // Al intentar aceptar la orden con un email distinto, debe saltar la SecurityException
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
    entregadaVieja.setFechaIngreso(LocalDateTime.now().minusDays(10)); // prioridad ALTA si no filtramos por estado
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
    // 1. Preparación
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
    // Ejecución
    List<EstadoOrden> permitidos = servicioOrdenReparacion.obtenerEstadosPermitidosPara(
      EstadoOrden.PRESUPUESTO_ACEPTADO
    );

    // Validación
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
    // Ejecución
    List<EstadoOrden> permitidos = servicioOrdenReparacion.obtenerEstadosPermitidosPara(
      EstadoOrden.EN_REPARACION
    );

    // Validación
    assertThat(
      permitidos,
      hasItems(EstadoOrden.EN_REPARACION, EstadoOrden.REPARADO, EstadoOrden.ESPERANDO_REPUESTO)
    );
    assertThat(permitidos.size(), equalTo(3));
  }

  @Test
  public void obtenerEstadosPermitidosParaEsperandoRepuestoDeberiaPermitirVolverAEnReparacionOReparado() {
    // Ejecución
    List<EstadoOrden> permitidos = servicioOrdenReparacion.obtenerEstadosPermitidosPara(
      EstadoOrden.ESPERANDO_REPUESTO
    );

    // Validación
    assertThat(
      permitidos,
      hasItems(EstadoOrden.ESPERANDO_REPUESTO, EstadoOrden.REPARADO, EstadoOrden.EN_REPARACION)
    );
    assertThat(permitidos.size(), equalTo(3));
  }

  @Test
  public void deberiaRetornarCorrectamenteEstadosPermitidosParaLaSolicitudDePresupuesto() {
    EstadoOrden estadoActual = EstadoOrden.SOLICITUD_COTIZACION;

    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(estadoActual);

    assertEquals(2, permitidos.size());
    assertThat(
      permitidos,
      containsInAnyOrder(EstadoOrden.SOLICITUD_COTIZACION, EstadoOrden.COTIZACION_PREVIA_ENVIADA)
    );
  }

  @Test
  public void deberiaRetornarCorrectamenteEstadosPermitidosParaCotizacionPreviaEnviada() {
    EstadoOrden estadoActual = EstadoOrden.COTIZACION_PREVIA_ENVIADA;

    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(estadoActual);

    assertEquals(3, permitidos.size());
    assertThat(
      permitidos,
      containsInAnyOrder(
        EstadoOrden.COTIZACION_PREVIA_ENVIADA,
        EstadoOrden.ESPERANDO_INGRESO,
        EstadoOrden.COTIZACION_DESCARTADA
      )
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
    orden.setEstado(EstadoOrden.RECIBIDO); // Estado inválido para aceptar

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
    orden.setEstado(EstadoOrden.EN_DIAGNOSTICO); // Estado inválido para rechazar

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
    // Preparación: Entra una orden Nivel 2
    OrdenReparacion orden = new OrdenReparacion("Juan", "123456", "PC Escritorio", "No da video");
    orden.setEmailCliente("juan@test.com"); // <- AGREGAMOS EL EMAIL PARA PASAR LA VALIDACIÓN
    orden.setNivelReparacion(NivelReparacion.NIVEL_2);

    // Tenemos 3 técnicos con distintos niveles
    Usuario tecNivel1 = new Usuario();
    tecNivel1.setId(1L);
    tecNivel1.setNivelSoporte(NivelReparacion.NIVEL_1);

    Usuario tecNivel2 = new Usuario();
    tecNivel2.setId(2L);
    tecNivel2.setNivelSoporte(NivelReparacion.NIVEL_2); // Este es el indicado

    Usuario tecNivel3 = new Usuario();
    tecNivel3.setId(3L);
    tecNivel3.setNivelSoporte(NivelReparacion.NIVEL_3);

    // Simulamos que el repositorio devuelve a los tres
    when(this.repositorioUsuarioMock.buscarTecnicosActivos())
      .thenReturn(List.of(tecNivel1, tecNivel2, tecNivel3));

    // Simulamos que la carga de trabajo no es problema (todos en 0)
    when(this.repositorioOrdenReparacionMock.contarOrdenesActivas(anyLong())).thenReturn(0L);

    // Ejecución
    this.servicioOrdenReparacion.registrarOrden(orden);

    // Validación: El sistema debió saltear al técnico 1 y al 3, y elegir al 2
    assertThat(orden.getTecnicoAsignado(), is(notNullValue()));
    assertThat(orden.getTecnicoAsignado().getId(), is(equalTo(2L)));
    assertThat(orden.getTecnicoAsignado().getNivelSoporte(), is(equalTo(NivelReparacion.NIVEL_2)));
  }

  @Test
  public void deberiaLanzarExcepcionSiNoHayTecnicosConElNivelRequeridoParaLaOrden() {
    // Preparación: Entra una orden muy compleja (Nivel 3)
    OrdenReparacion orden = new OrdenReparacion("María", "654321", "Servidor", "Falla placa base");
    orden.setEmailCliente("maria@test.com"); // <- AGREGAMOS EL EMAIL PARA PASAR LA VALIDACIÓN
    orden.setNivelReparacion(NivelReparacion.NIVEL_3);

    // Pero solo tenemos técnicos Nivel 1 y 2 en el taller
    Usuario tecNivel1 = new Usuario();
    tecNivel1.setId(1L);
    tecNivel1.setNivelSoporte(NivelReparacion.NIVEL_1);

    Usuario tecNivel2 = new Usuario();
    tecNivel2.setId(2L);
    tecNivel2.setNivelSoporte(NivelReparacion.NIVEL_2);

    when(this.repositorioUsuarioMock.buscarTecnicosActivos())
      .thenReturn(List.of(tecNivel1, tecNivel2));

    // Ejecución y Validación: Tiene que frenar la asignación y lanzar la excepción
    assertThrows(
      NoHayTecnicosDisponibles.class,
      () -> {
        this.servicioOrdenReparacion.registrarOrden(orden);
      }
    );
  }
}
