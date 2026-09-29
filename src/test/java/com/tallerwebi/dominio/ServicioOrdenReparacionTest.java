package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.OrdenNoEncontrado;
import com.tallerwebi.presentacion.DatosOrden;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioOrdenReparacionTest {

  private ServicioOrdenReparacion servicioOrdenReparacion;
  private RepositorioOrdenReparacion repositorioOrdenReparacionMock;

  @BeforeEach
  public void init() {
    this.repositorioOrdenReparacionMock = mock(RepositorioOrdenReparacion.class);
    this.servicioOrdenReparacion =
      new ServicioOrdenReparacionImpl(this.repositorioOrdenReparacionMock);
  }

  @Test
  public void queSeRegistreUnaOrdenReparacionYSeEnvieAGuardarEnElRepositorio()
    throws DatosIncompletosException {
    DatosOrden datosOrdenMock = mock(DatosOrden.class);

    when(datosOrdenMock.getNombreCliente()).thenReturn("test");
    when(datosOrdenMock.getTelefonoCliente()).thenReturn("test");
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

  //---------------------------
  @Test
  public void buscarPorIdDeberiaLlamarABuscarIdOrdenReparacion() {
    Long id = 1L;
    OrdenReparacion ordenEsperado = new OrdenReparacion();
    when(this.repositorioOrdenReparacionMock.buscarPorIdOrdenReparacion(id))
      .thenReturn(ordenEsperado);

    OrdenReparacion ordenEncontrado = this.servicioOrdenReparacion.buscarPorId(id);

    assertThat(ordenEncontrado, equalTo(ordenEsperado));
    verify(this.repositorioOrdenReparacionMock, times(1)).buscarPorIdOrdenReparacion(id);
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
  public void deberiaActualizarEstadoYNotaTecnicaFuncionesCorrectamente() {
    Long id = 1L;
    EstadoOrden nuevoEstado = EstadoOrden.REPARADO;
    String notaTecnica = "Se cambio la pantalla";

    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(id);
    orden.setEstado(EstadoOrden.ESPERANDO_REPUESTO);

    when(repositorioOrdenReparacionMock.buscarPorIdOrdenReparacion(id)).thenReturn(orden);

    this.servicioOrdenReparacion.actualizarEstadoYNotaTecnica(id, nuevoEstado, notaTecnica);

    assertThat(orden.getEstado(), equalTo(nuevoEstado));
    assertThat(orden.getNotaTecnica(), equalTo(notaTecnica));

    verify(repositorioOrdenReparacionMock, times(1)).buscarPorIdOrdenReparacion(id);
  }

  @Test
  public void queLanzeExcepcionOrdenNoEncontradoAlIntentarActualizarUnaOrdenInexistente() {
    Long idInexistente = 99L;
    when(repositorioOrdenReparacionMock.buscarPorIdOrdenReparacion(idInexistente)).thenReturn(null);

    // Verificamos que salte tu excepción
    assertThrows(
      OrdenNoEncontrado.class,
      () -> {
        servicioOrdenReparacion.actualizarEstadoYNotaTecnica(
          idInexistente,
          EstadoOrden.ENTREGADO,
          "TestNota"
        );
      }
    );
    verify(repositorioOrdenReparacionMock, times(1)).buscarPorIdOrdenReparacion(idInexistente);
  }
}
