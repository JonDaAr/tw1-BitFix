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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorOrdenReparacionTest {

  private ControladorOrdenReparacion controladorOrdenReparacion;
  private ServicioOrdenReparacion servicioOrdenReparacionMock;
  private OrdenReparacion ordenReparacionMock;
  private DatosOrden datosOrdenMock;
  private HttpSession sessionMock;
  private Usuario usuarioMock; // Agregado para usar en varios tests

  private DatosOrden crearDatos() {
    DatosOrden datos = new DatosOrden();
    datos.setNombreCliente("Juan Perez");
    datos.setEmailCliente("juan@test.com");
    datos.setModeloEquipo("Notebook Dell");
    datos.setDescripcionFalla("No enciende");
    datos.setAccesorios("Cargador");
    return datos;
  }

  private Usuario crearClienteEnSesion() {
    Usuario cliente = new Usuario();
    cliente.setEmail("cliente@mail.com");
    when(sessionMock.getAttribute("USUARIO")).thenReturn(cliente);
    return cliente;
  }

  @BeforeEach
  public void init() {
    servicioOrdenReparacionMock = mock(ServicioOrdenReparacion.class);
    controladorOrdenReparacion = new ControladorOrdenReparacion(servicioOrdenReparacionMock);
    ordenReparacionMock = mock(OrdenReparacion.class);
    datosOrdenMock = mock(DatosOrden.class);
    sessionMock = mock(HttpSession.class);
    usuarioMock = mock(Usuario.class);
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

    when(servicioOrdenReparacionMock.obtenerOrdenesParaTecnico()).thenReturn(List.of());

    ModelAndView mav = controladorOrdenReparacion.registrarOrdenDesdeRecepcion(datosOrdenMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("panel-tecnico"));
    assertThat(mav.getModel().get("vista"), equalTo("nueva-orden"));
    assertThat(mav.getModel().get("mostrarConfirmacion"), equalTo(true));
    assertThat(mav.getModel().get("orden"), equalTo(ordenReparacionMock));

    verify(servicioOrdenReparacionMock).registrarOrden(any(OrdenReparacion.class));
    verify(servicioOrdenReparacionMock).obtenerOrdenesParaTecnico();
  }

  @Test
  public void queMuestreErrorDatosIncompletosEnRecepcion() throws DatosIncompletosException {
    doThrow(DatosIncompletosException.class)
      .when(servicioOrdenReparacionMock)
      .registrarOrden(any(OrdenReparacion.class));

    ModelAndView mav = controladorOrdenReparacion.registrarOrdenDesdeRecepcion(datosOrdenMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("panel-tecnico"));
    assertThat(mav.getModel().get("vista"), equalTo("nueva-orden"));
    assertThat(
      mav.getModel().get("error"),
      equalTo("Por favor, complete todos los campos obligatorios.")
    );
    assertThat(mav.getModel().get("datosOrden"), equalTo(datosOrdenMock));
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
  public void queMuestreLaVistaDeRecepcionHome() {
    crearClienteEnSesion();

    ModelAndView mav = controladorOrdenReparacion.mostrarRecepcionHome(sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("recepcion-home"));
    assertThat(mav.getModel().get("datosOrden"), org.hamcrest.Matchers.notNullValue());
  }

  @Test
  public void queRegistreOrdenDesdeHomeCorrectamente()
    throws DatosIncompletosException, NoHayTecnicosDisponibles {
    crearClienteEnSesion();
    DatosOrden datos = crearDatos();

    when(servicioOrdenReparacionMock.registrarOrden(any(OrdenReparacion.class)))
      .thenReturn(new OrdenReparacion());
    DatosOrden datosOrden = new DatosOrden();

    ModelAndView mav = controladorOrdenReparacion.registrarOrdenDesdeHome(datosOrden, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/home"));
    verify(servicioOrdenReparacionMock).registrarOrden(any(OrdenReparacion.class));
  }

  @Test
  public void queMuestreErrorSiNoHayTecnicosDisponiblesEnRecepcionHome()
    throws DatosIncompletosException, NoHayTecnicosDisponibles {
    DatosOrden datosOrden = crearDatos();

    doThrow(NoHayTecnicosDisponibles.class)
      .when(servicioOrdenReparacionMock)
      .registrarOrden(any(OrdenReparacion.class));

    when(usuarioMock.getEmail()).thenReturn("cliente@mail.com");
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    ModelAndView mav = controladorOrdenReparacion.registrarOrdenDesdeHome(datosOrden, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("recepcion-home"));
    assertThat(
      mav.getModel().get("error").toString(),
      equalTo("No hay técnicos disponibles para asignar la orden.")
    );
    assertThat(mav.getModel().get("datosOrden"), equalTo(datosOrden));
  }

  @Test
  public void queMuestreErrorSiLosDatosEstanIncompletosEnRecepcionHome()
    throws DatosIncompletosException, NoHayTecnicosDisponibles {
    DatosOrden datos = crearDatos();

    doThrow(DatosIncompletosException.class)
      .when(servicioOrdenReparacionMock)
      .registrarOrden(any(OrdenReparacion.class));

    DatosOrden datosOrden = new DatosOrden();

    when(usuarioMock.getEmail()).thenReturn("cliente@mail.com");
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    ModelAndView mav = controladorOrdenReparacion.registrarOrdenDesdeHome(datosOrden, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("recepcion-home"));
    assertThat(
      mav.getModel().get("error").toString(),
      equalTo("Por favor, complete todos los campos obligatorios.")
    );
    assertThat(mav.getModel().get("datosOrden"), equalTo(datosOrden));
  }

  @Test
  public void siElUsuarioNoEstaLogueadoAlVerFacturasDeberiaRedirigirALogin() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

    ModelAndView mav = controladorOrdenReparacion.verMisFacturas(sessionMock);

    assertThat(mav.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void siElUsuarioEstaLogueadoDeberiaVerSoloOrdenesReparadasOEntregadas() {
    when(usuarioMock.getEmail()).thenReturn("cliente@mail.com");
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    OrdenReparacion ordenRecibida = new OrdenReparacion();
    ordenRecibida.setEstado(EstadoOrden.RECIBIDO);

    OrdenReparacion ordenReparada = new OrdenReparacion();
    ordenReparada.setEstado(EstadoOrden.REPARADO);

    OrdenReparacion ordenEntregada = new OrdenReparacion();
    ordenEntregada.setEstado(EstadoOrden.ENTREGADO);

    List<OrdenReparacion> todasLasOrdenes = new ArrayList<>();
    todasLasOrdenes.add(ordenRecibida);
    todasLasOrdenes.add(ordenReparada);
    todasLasOrdenes.add(ordenEntregada);

    when(servicioOrdenReparacionMock.obtenerOrdenesDelCliente("cliente@mail.com"))
      .thenReturn(todasLasOrdenes);

    ModelAndView mav = controladorOrdenReparacion.verMisFacturas(sessionMock);

    assertThat(mav.getViewName(), equalTo("facturas-cliente"));
    List<OrdenReparacion> facturas = (List<OrdenReparacion>) mav.getModel().get("facturas");
    assertThat(facturas.size(), equalTo(2));
    assertThat(facturas, containsInAnyOrder(ordenReparada, ordenEntregada));
    verify(servicioOrdenReparacionMock, times(1)).obtenerOrdenesDelCliente("cliente@mail.com");
  }

  @Test
  public void queElEmailVengaCargadoDesdeLaSesionAlMostrarRecepcionHome() {
    Usuario cliente = crearClienteEnSesion();

    ModelAndView mav = controladorOrdenReparacion.mostrarRecepcionHome(sessionMock);

    DatosOrden datos = (DatosOrden) mav.getModel().get("datosOrden");
    assertThat(datos.getEmailCliente(), is(equalTo(cliente.getEmail())));
  }

  @Test
  public void queSeRegistreLaOrdenConElEmailCorrespondienteDeLaSesion()
    throws DatosIncompletosException, NoHayTecnicosDisponibles {
    Usuario cliente = crearClienteEnSesion();
    DatosOrden datos = crearDatos();

    when(servicioOrdenReparacionMock.registrarOrden(any(OrdenReparacion.class)))
      .thenReturn(new OrdenReparacion());

    ModelAndView mav = controladorOrdenReparacion.registrarOrdenDesdeHome(datos, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/home"));
    assertThat(datos.getEmailCliente(), is(equalTo(cliente.getEmail())));
    verify(servicioOrdenReparacionMock).registrarOrden(any(OrdenReparacion.class));
  }

  @Test
  public void siElUsuarioNoEstaLogueadoAlIntentarVerElComprobanteDebeRedirigirAlLogin()
    throws PedidoNoEncontradoException {
    Integer codigoSeguimiento = 123456;
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

    ModelAndView mav = controladorOrdenReparacion.verComprobante(codigoSeguimiento, sessionMock);

    assertThat(mav.getViewName(), equalTo("redirect:/login"));
    verify(servicioOrdenReparacionMock, never()).consultarEstado(codigoSeguimiento);
  }

  @Test
  public void siElComprobanteNoExisteDebeRedirigirAMisFacturas()
    throws PedidoNoEncontradoException {
    Integer codigoSeguimiento = 999999;
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(servicioOrdenReparacionMock.consultarEstado(codigoSeguimiento))
      .thenThrow(new PedidoNoEncontradoException("No encontrado"));

    ModelAndView mav = controladorOrdenReparacion.verComprobante(codigoSeguimiento, sessionMock);

    assertThat(mav.getViewName(), equalTo("redirect:/mis-facturas"));
  }

  @Test
  public void siElUsuarioNoEsElDuenioDelComprobanteDebeRedirigirAMisFacturas()
    throws PedidoNoEncontradoException {
    Integer codigoSeguimiento = 123456;
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getEmail()).thenReturn("infiltrado@mail.com");

    OrdenReparacion ordenAjena = new OrdenReparacion();
    ordenAjena.setEmailCliente("dueñoreal@mail.com");

    when(servicioOrdenReparacionMock.consultarEstado(codigoSeguimiento)).thenReturn(ordenAjena);

    ModelAndView mav = controladorOrdenReparacion.verComprobante(codigoSeguimiento, sessionMock);

    assertThat(mav.getViewName(), equalTo("redirect:/mis-facturas"));
  }

  @Test
  public void siElComprobanteExisteYElUsuarioEsElDuenioDebeMostrarLaVistaComprobanteConLosDatos()
    throws PedidoNoEncontradoException {
    Integer codigoSeguimiento = 123456;
    String emailUsuario = "cliente@mail.com";

    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getEmail()).thenReturn(emailUsuario);

    OrdenReparacion ordenPropia = new OrdenReparacion();
    ordenPropia.setEmailCliente(emailUsuario);

    when(servicioOrdenReparacionMock.consultarEstado(codigoSeguimiento)).thenReturn(ordenPropia);

    ModelAndView mav = controladorOrdenReparacion.verComprobante(codigoSeguimiento, sessionMock);

    assertThat(mav.getViewName(), equalTo("comprobante"));
    assertThat(mav.getModel().get("orden"), notNullValue());
    assertThat(mav.getModel().get("orden"), equalTo(ordenPropia));
  }
}
