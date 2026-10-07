package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DatosOrden;
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
    DatosOrden datosOrdenMock = mock(DatosOrden.class);

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
    DatosOrden datosOrdenMock = mock(DatosOrden.class);

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
    DatosOrden datosOrden = null;

    assertThrows(
      DatosIncompletosException.class,
      () -> this.servicioOrdenReparacion.registrarOrden(datosOrden)
    );
  }

  @Test
  public void queNoSePuedaRegistrarUnaOrdenReparacionIncompletaYLanceExcepcion() {
    DatosOrden datosOrdenMock = mock(DatosOrden.class);

    when(datosOrdenMock.getNombreCliente()).thenReturn("test");
    when(datosOrdenMock.getTelefonoCliente()).thenReturn("test");
    //falta completar modelo de equipo
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

  //-----test gestion estados y diagnostico(nota tecnica)

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

    assertEquals(2, permitidos.size());
    assertThat(
      permitidos,
      containsInAnyOrder(EstadoOrden.ESPERANDO_REPUESTO, EstadoOrden.REPARADO)
    );
  }

  @Test
  public void deberiaRetornarSoloElMismoEstadoSiEsReparado() {
    EstadoOrden estadoActual = EstadoOrden.REPARADO;

    List<EstadoOrden> permitidos =
      this.servicioOrdenReparacion.obtenerEstadosPermitidosPara(estadoActual);

    assertEquals(1, permitidos.size());
  }

  @Test
  public void deberiaRechazarPresupuestoYCambiarEstado() {
    Integer codigo = 123456;
    OrdenReparacion orden = new OrdenReparacion();
    orden.setEstado(EstadoOrden.PRESUPUESTO_ENVIADO);

    when(repositorioOrdenReparacionMock.buscarPorCodigo(codigo)).thenReturn(orden);

    servicioOrdenReparacion.rechazarPresupuesto(codigo);

    assertThat(orden.getEstado(), equalTo(EstadoOrden.PRESUPUESTO_RECHAZADO));
    verify(repositorioOrdenReparacionMock, times(1)).modificarOrden(orden);
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
}
