package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

public class ControladorOrdenReparacionTest {

  private ControladorOrdenReparacion controladorOrdenReparacion;
  private ServicioOrdenReparacion servicioOrdenReparacionMock;
  private OrdenReparacion ordenReparacionMock;
  private DatosOrden datosOrdenMock;

  @BeforeEach
  public void init() {
    servicioOrdenReparacionMock = mock(ServicioOrdenReparacion.class);
    controladorOrdenReparacion = new ControladorOrdenReparacion(servicioOrdenReparacionMock);
    ordenReparacionMock = mock(OrdenReparacion.class);
    datosOrdenMock = mock(DatosOrden.class);
  }

  @Test
  public void queSePuedaIrAlRegistroOrdenReparacion() {
    ModelAndView modelAndView = controladorOrdenReparacion.irAFormularioRegistrarOrdenReparacion();

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("registro-orden-reparacion"));
  }

  @Test
  public void queSePuedaRegistrarUnaOrdenReparacionValida() throws DatosIncompletosException {
    when(servicioOrdenReparacionMock.registrarOrden(datosOrdenMock))
      .thenReturn(ordenReparacionMock);

    ModelAndView modelAndView = controladorOrdenReparacion.registrarOrdenReparacion(datosOrdenMock);

    verify(servicioOrdenReparacionMock, times(1)).registrarOrden(datosOrdenMock);
  }

  @Test
  public void queMuestreVistaDeConfirmacionAlRegistrarUnaOrdenReparacionValida()
    throws DatosIncompletosException {
    when(servicioOrdenReparacionMock.registrarOrden(datosOrdenMock))
      .thenReturn(ordenReparacionMock);

    ModelAndView modelAndView = controladorOrdenReparacion.registrarOrdenReparacion(datosOrdenMock);

    servicioOrdenReparacionMock.registrarOrden(datosOrdenMock);

    assertThat(
      modelAndView.getViewName(),
      equalToIgnoringCase("confirmacion-nueva-orden-reparacion")
    );
  }

  @Test
  public void queNoSePuedaRegistrarUnaOrdenReparacionIncompletaYMuestreMensajeDeError()
    throws DatosIncompletosException {
    doThrow(DatosIncompletosException.class)
      .when(servicioOrdenReparacionMock)
      .registrarOrden(datosOrdenMock);

    ModelAndView modelAndView = controladorOrdenReparacion.registrarOrdenReparacion(datosOrdenMock);

    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase(
        "No se puede registrar una orden con datos incompletos. Por favor, complete todos los campos."
      )
    );
  }

  @Test
  public void queNoSePuedaRegistrarUnaOrdenReparacionIncompletaYLleveALaVistaDelFormularioNuevamente()
    throws DatosIncompletosException {
    doThrow(DatosIncompletosException.class)
      .when(servicioOrdenReparacionMock)
      .registrarOrden(datosOrdenMock);

    ModelAndView modelAndView = controladorOrdenReparacion.registrarOrdenReparacion(datosOrdenMock);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("registro-orden-reparacion"));
  }

  @Test
  public void queLaVistaDeConfirmacionMuestreElCodigoDeSeguimientoAlRegistrarOrdenReparacionValida()
    throws DatosIncompletosException {
    when(servicioOrdenReparacionMock.registrarOrden(datosOrdenMock))
      .thenReturn(ordenReparacionMock);
    when(ordenReparacionMock.getCodigoSeguimiento()).thenReturn(123456);

    ModelAndView modelAndView = controladorOrdenReparacion.registrarOrdenReparacion(datosOrdenMock);

    assertThat(
      modelAndView.getModel().get("codigoSeguimiento").toString(),
      equalToIgnoringCase(String.valueOf(123456))
    );
  }
}
