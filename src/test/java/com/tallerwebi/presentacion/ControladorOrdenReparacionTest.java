package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.EstadoOrden;
import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.OrdenNoEncontrado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

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


  //-------------
  @Test
  public void irALaListaDeOrdenesDeberiaRetornarVistaListaOrdenesConLaLista() {
    List<OrdenReparacion> listaMock = mock(List.class);
    when(this.servicioOrdenReparacionMock.listarTodas()).thenReturn(listaMock);

    ModelAndView modelAndView = this.controladorOrdenReparacion.irALaListaDeOrdenes();

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("lista-ordenes"));
    assertThat(modelAndView.getModel().get("ordenes"), instanceOf(List.class));
  }

  @Test
  public void irAEditarOrdenDeberiaRetornarVistaEditarOrdenConDatosYEstados() {
    OrdenReparacion ordenMock = mock(OrdenReparacion.class);
    when(this.servicioOrdenReparacionMock.buscarPorId(anyLong())).thenReturn(ordenMock);

    ModelAndView modelAndView = this.controladorOrdenReparacion.irAEditarOrden(1L);

    assertThat(modelAndView.getViewName(),equalToIgnoringCase("editar-orden"));
    assertThat(modelAndView.getModel().get("ordenAActualizar"), notNullValue());
    assertThat(modelAndView.getModel().get("estados"), instanceOf(EstadoOrden[].class));

    verify(this.servicioOrdenReparacionMock, times(1)).buscarPorId(anyLong());
  }

  @Test
  public void actualizarOrdenExitosamenteDeberiaRedirigirALaListaDeOrdenes() {

    ActualizacionOrden actualizacion = new ActualizacionOrden(1L, "modelo", EstadoOrden.ESPERANDO_REPUESTO, "nota");

    //Simula que el servicio no lanza ninguna excepcion
    doNothing().when(this.servicioOrdenReparacionMock)
            .actualizarEstadoYNotaTecnica(anyLong(), any(), anyString());

    ModelAndView modelAndView = this.controladorOrdenReparacion.actualizarOrden(actualizacion);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/ordenes"));
    verify(this.servicioOrdenReparacionMock, times(1))
            .actualizarEstadoYNotaTecnica(1L, EstadoOrden.ESPERANDO_REPUESTO, "nota");
  }

  @Test
  public void actualizarOrdenInexistenteDeberiaRetornarVistaEditarOrdenConError() {
    ActualizacionOrden actualizacion = new ActualizacionOrden(11L,
            "modelo",
            EstadoOrden.ESPERANDO_REPUESTO,
            "nota");

    //Simula que el servicio lanza la excepción OrdenNoEncontrado
    doThrow(OrdenNoEncontrado.class).when(this.servicioOrdenReparacionMock)
            .actualizarEstadoYNotaTecnica(anyLong(), any(), anyString());

    ModelAndView modelAndView = this.controladorOrdenReparacion.actualizarOrden(actualizacion);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("editar-orden"));
    assertThat(modelAndView.getModel().get("error"), notNullValue());
    assertThat(modelAndView.getModel().get("ordenAActualizar"), equalTo(actualizacion));
    assertThat(modelAndView.getModel().get("estados"), instanceOf(EstadoOrden[].class));
  }

}
