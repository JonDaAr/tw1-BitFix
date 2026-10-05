package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.EstadoOrden;
import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import java.util.List;
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
    when(servicioOrdenReparacionMock.registrarOrden(any(OrdenReparacion.class)))
      .thenReturn(ordenReparacionMock);

    ModelAndView modelAndView = controladorOrdenReparacion.registrarOrdenReparacion(datosOrdenMock);

    verify(servicioOrdenReparacionMock, times(1)).registrarOrden(any(OrdenReparacion.class));
  }

  @Test
  public void queMuestreVistaDeConfirmacionAlRegistrarUnaOrdenReparacionValida()
    throws DatosIncompletosException {
    when(servicioOrdenReparacionMock.registrarOrden(any(OrdenReparacion.class)))
      .thenReturn(ordenReparacionMock);

    ModelAndView modelAndView = controladorOrdenReparacion.registrarOrdenReparacion(datosOrdenMock);

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
      .registrarOrden(any(OrdenReparacion.class));

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
      .registrarOrden(any(OrdenReparacion.class));

    ModelAndView modelAndView = controladorOrdenReparacion.registrarOrdenReparacion(datosOrdenMock);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("registro-orden-reparacion"));
  }

  @Test
  public void queLaVistaDeConfirmacionMuestreElCodigoDeSeguimientoAlRegistrarOrdenReparacionValida()
    throws DatosIncompletosException {
    when(servicioOrdenReparacionMock.registrarOrden(any(OrdenReparacion.class)))
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
    DatosOrden datos = new DatosOrden();
    datos.setCodigoSeguimiento(123456);

    OrdenReparacion ordenFake = new OrdenReparacion("Juan", "11223344", "Moto G", "Pantalla rota");
    when(servicioOrdenReparacionMock.consultarEstado(123456)).thenReturn(ordenFake);

    ModelAndView mav = controladorOrdenReparacion.buscarEstado(datos);

    assertThat(mav.getViewName(), is(equalTo("resultado-consulta")));
    assertThat(mav.getModel().get("pedido"), is(equalTo(ordenFake)));
  }

  @Test
  public void queMuestreMensajeDeErrorSiElPedidoNoExiste() throws PedidoNoEncontradoException {
    DatosOrden datos = new DatosOrden();
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

  @Test
  public void queMuestreLaVistaDeRecepcion() {
    ModelAndView mav = controladorOrdenReparacion.mostrarRecepcion();
    assertThat(mav.getViewName(), equalToIgnoringCase("recepcion"));
    assertThat(mav.getModel().get("datosOrden"), is(notNullValue()));
  }

  @Test
  public void queRegistreOrdenDesdeRecepcionCorrectamente()
    throws DatosIncompletosException, NoHayTecnicosDisponibles {
    when(servicioOrdenReparacionMock.registrarOrden(any(OrdenReparacion.class)))
      .thenReturn(ordenReparacionMock);
    ModelAndView mav = controladorOrdenReparacion.registrarOrdenDesdeRecepcion(datosOrdenMock);
    assertThat(mav.getViewName(), equalToIgnoringCase("confirmacion-nueva-orden-reparacion"));
    verify(servicioOrdenReparacionMock).registrarOrden(any(OrdenReparacion.class));
  }

  @Test
  public void queMuestreErrorDatosIncompletosEnRecepcion() throws DatosIncompletosException {
    doThrow(DatosIncompletosException.class)
      .when(servicioOrdenReparacionMock)
      .registrarOrden(any(OrdenReparacion.class));
    ModelAndView mav = controladorOrdenReparacion.registrarOrdenDesdeRecepcion(datosOrdenMock);
    assertThat(mav.getViewName(), equalToIgnoringCase("recepcion"));
    assertThat(
      mav.getModel().get("error"),
      equalTo("Por favor, complete todos los campos obligatorios.")
    );
  }

  @Test
  public void queMuestreErrorSiCodigoSeguimientoEsNulo() {
    DatosOrden datos = new DatosOrden();

    ModelAndView mav = controladorOrdenReparacion.buscarEstado(datos);
    assertThat(mav.getViewName(), equalTo("consultar-estado"));
    assertThat(
      mav.getModel().get("error"),
      equalTo("Por favor, ingrese un código de seguimiento.")
    );
  }

  @Test
  public void queMuestreErrorSiDatosEsNull() {
    ModelAndView mav = controladorOrdenReparacion.buscarEstado(null);
    assertThat(mav.getViewName(), equalTo("consultar-estado"));
    assertNotNull(mav.getModel().get("datosConsulta"));
  }

  @Test
  public void queListeTodasLasOrdenes() {
    when(servicioOrdenReparacionMock.listarTodas()).thenReturn(List.of(new OrdenReparacion()));
    ModelAndView mav = controladorOrdenReparacion.irALaListaDeOrdenes();
    assertThat(mav.getViewName(), equalTo("lista-ordenes"));
    assertNotNull(mav.getModel().get("ordenes"));
  }

  @Test
  public void queCargueOrdenParaEditar() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(1L);
    orden.setModeloEquipo("Notebook");
    orden.setEstado(EstadoOrden.EN_DIAGNOSTICO);
    orden.setNotaTecnica("Revision");
    when(servicioOrdenReparacionMock.buscarPorId(1L)).thenReturn(orden);
    ModelAndView mav = controladorOrdenReparacion.irAEditarOrden(1L);
    assertThat(mav.getViewName(), equalTo("editar-orden"));
    assertNotNull(mav.getModel().get("ordenAActualizar"));
  }

  @Test
  public void queActualiceOrdenCorrectamente() {
    ActualizacionOrden actualizacion = new ActualizacionOrden(
      1L,
      "Notebook",
      EstadoOrden.EN_DIAGNOSTICO,
      "OK"
    );
    ModelAndView mav = controladorOrdenReparacion.actualizarOrden(actualizacion);
    assertThat(mav.getViewName(), equalTo("redirect:/tecnico/panel-tecnico"));
    verify(servicioOrdenReparacionMock)
      .actualizarEstadoYNotaTecnica(1L, EstadoOrden.EN_DIAGNOSTICO, "OK");
  }

  //-----test gestion estados y diagnostico(nota tecnica)

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

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("editar-orden"));
    assertThat(modelAndView.getModel().get("ordenAActualizar"), notNullValue());
    assertThat(modelAndView.getModel().get("estados"), instanceOf(List.class));

    verify(this.servicioOrdenReparacionMock, times(1)).buscarPorId(anyLong());
  }

  @Test
  public void actualizarOrdenExitosamenteDeberiaRedirigirALaListaDeOrdenes() {
    ActualizacionOrden actualizacion = new ActualizacionOrden(
      1L,
      "modelo",
      EstadoOrden.ESPERANDO_REPUESTO,
      "nota"
    );
    ModelAndView modelAndView = this.controladorOrdenReparacion.actualizarOrden(actualizacion);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/tecnico/panel-tecnico"));
    verify(this.servicioOrdenReparacionMock, times(1))
      .actualizarEstadoYNotaTecnica(
        actualizacion.getIdOrdenReparacion(),
        actualizacion.getEstado(),
        actualizacion.getNotaTecnica()
      );
  }

  @Test
  public void quePermitaBuscarEstadoPorGetConCodigoValido() throws Exception {
    OrdenReparacion ordenEsperada = new OrdenReparacion();
    ordenEsperada.setCodigoSeguimiento(123456);

    when(servicioOrdenReparacionMock.consultarEstado(123456)).thenReturn(ordenEsperada);

    ModelAndView mav = controladorOrdenReparacion.buscarEstadoPorGet(123456);

    assertThat(mav.getViewName(), equalTo("resultado-consulta"));
    assertThat(mav.getModel().get("pedido"), equalTo(ordenEsperada));
  }
}
