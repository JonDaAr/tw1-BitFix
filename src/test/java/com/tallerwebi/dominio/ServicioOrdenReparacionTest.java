package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.presentacion.DatosOrden;
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
}
