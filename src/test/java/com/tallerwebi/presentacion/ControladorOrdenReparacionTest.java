package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

  //  SECCIÓN 2: CONSULTA PÚBLICA DE ESTADO
  // ==========================================

  @Test
  public void queMuestreLaVistaDeBusquedaAlNavegarAConsultarEstado() {
    ModelAndView mav = controladorOrdenReparacion.irAConsulta();

    assertThat(mav.getViewName(), is(equalTo("consultar-estado")));
    assertThat(mav.getModel().get("datosConsulta"), is(notNullValue()));
  }

  @Test
  public void queRetorneLaVistaResultadoSiElPedidoExiste() throws PedidoNoEncontradoException {
    DatosConsultaEstado datos = new DatosConsultaEstado();
    datos.setCodigoSeguimiento(123456);

    OrdenReparacion ordenFake = new OrdenReparacion("Juan", "11223344", "Moto G", "Pantalla rota");
    when(servicioOrdenReparacionMock.consultarEstado(123456)).thenReturn(ordenFake);

    ModelAndView mav = controladorOrdenReparacion.buscarEstado(datos);

    assertThat(mav.getViewName(), is(equalTo("resultado-consulta")));
    assertThat(mav.getModel().get("pedido"), is(equalTo(ordenFake)));
  }

  @Test
  public void queMuestreMensajeDeErrorSiElPedidoNoExiste() throws PedidoNoEncontradoException {
    DatosConsultaEstado datos = new DatosConsultaEstado();
    datos.setCodigoSeguimiento(999999);

    when(servicioOrdenReparacionMock.consultarEstado(999999))
      .thenThrow(
        new PedidoNoEncontradoException("No se encontró ningún pedido con el código ingresado")
      );

    ModelAndView mav = controladorOrdenReparacion.buscarEstado(datos);

    assertThat(mav.getViewName(), is(equalTo("consultar-estado")));
    assertThat(
      mav.getModel().get("error"),
      is(equalTo("No se encontró ningún pedido con el código ingresado"))
    );
  }
}
