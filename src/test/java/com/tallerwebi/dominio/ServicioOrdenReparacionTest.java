package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DatosOrden;
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
}
