package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.dominio.excepcion.SinStockException;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorPresupuestoTest {

  private ServicioPresupuesto servicioPresupuestoMock;
  private ServicioOrdenReparacion servicioOrdenReparacionMock;
  private ControladorPresupuesto controladorPresupuesto;
  private ServicioOrdenReparacion servicioOrdenReparacion;
  private ServicioAlerta servicioAlertaMock;

  @BeforeEach
  public void init() {
    this.servicioPresupuestoMock = mock(ServicioPresupuesto.class);
    this.servicioOrdenReparacionMock = mock(ServicioOrdenReparacion.class);
    this.servicioAlertaMock = mock(ServicioAlerta.class);
    this.controladorPresupuesto =
      new ControladorPresupuesto(
        this.servicioPresupuestoMock,
        this.servicioOrdenReparacionMock,
        this.servicioAlertaMock
      );
  }

  @Test
  public void alAceptarPresupuesto_debeInvocarAlServicioYRedirigirAConsulta() {
    Integer codigoSeguimiento = 123456;
    String email = "cliente@mail.com";

    // 1. Simulamos la sesión y el usuario
    jakarta.servlet.http.HttpSession sessionMock = mock(jakarta.servlet.http.HttpSession.class);
    com.tallerwebi.dominio.Usuario usuarioMock = mock(com.tallerwebi.dominio.Usuario.class);

    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getEmail()).thenReturn(email);

    // 2. Le pasamos el mock de la sesión al controlador
    ModelAndView mav =
      this.controladorPresupuesto.aceptarPresupuesto(codigoSeguimiento, sessionMock);

    // 3. Verificamos que el servicio se llame con ambos parámetros y redirija al home
    verify(this.servicioOrdenReparacionMock, times(1)).aceptarPresupuesto(codigoSeguimiento, email);

    assertThat(mav.getViewName(), equalTo("redirect:/home?exito=presupuesto_aceptado"));
  }

  @Test
  public void alAceptarPresupuestoInexistente_debeMostrarError() {
    Integer codigoSeguimiento = 999999;
    String email = "cliente@mail.com";

    jakarta.servlet.http.HttpSession sessionMock = mock(jakarta.servlet.http.HttpSession.class);
    com.tallerwebi.dominio.Usuario usuarioMock = mock(com.tallerwebi.dominio.Usuario.class);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getEmail()).thenReturn(email);

    // Actualizamos el mock del servicio para que espere el email
    doThrow(new IllegalArgumentException("No se encontró la orden"))
      .when(this.servicioOrdenReparacionMock)
      .aceptarPresupuesto(codigoSeguimiento, email);

    ModelAndView mav =
      this.controladorPresupuesto.aceptarPresupuesto(codigoSeguimiento, sessionMock);

    assertThat(mav.getViewName(), equalTo("consultar-estado"));
    assertThat(mav.getModel().get("error"), equalTo("No se encontró la orden"));
  }

  @Test
  public void alRechazarPresupuesto_debeInvocarAlServicioYRedirigirAConsulta() {
    Integer codigoSeguimiento = 123456;
    String email = "cliente@mail.com";

    jakarta.servlet.http.HttpSession sessionMock = mock(jakarta.servlet.http.HttpSession.class);
    com.tallerwebi.dominio.Usuario usuarioMock = mock(com.tallerwebi.dominio.Usuario.class);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getEmail()).thenReturn(email);

    ModelAndView mav =
      this.controladorPresupuesto.rechazarPresupuesto(codigoSeguimiento, sessionMock);

    verify(this.servicioOrdenReparacionMock, times(1))
      .rechazarPresupuesto(codigoSeguimiento, email);
    assertThat(mav.getViewName(), equalTo("redirect:/home?exito=presupuesto_rechazado"));
  }

  @Test
  public void alRechazarPresupuestoInexistente_debeMostrarError() {
    Integer codigoSeguimiento = 999999;
    String email = "cliente@mail.com";

    jakarta.servlet.http.HttpSession sessionMock = mock(jakarta.servlet.http.HttpSession.class);
    com.tallerwebi.dominio.Usuario usuarioMock = mock(com.tallerwebi.dominio.Usuario.class);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getEmail()).thenReturn(email);

    doThrow(new IllegalArgumentException("No se encontró la orden"))
      .when(this.servicioOrdenReparacionMock)
      .rechazarPresupuesto(codigoSeguimiento, email);

    ModelAndView mav =
      this.controladorPresupuesto.rechazarPresupuesto(codigoSeguimiento, sessionMock);

    assertThat(mav.getViewName(), equalTo("consultar-estado"));
    assertThat(mav.getModel().get("error"), equalTo("No se encontró la orden"));
  }

  @Test
  public void alIrAVistaPresupuesto_debeMostrarLaVistaPresupuestoConListaDeDisponibles() {
    List<Repuesto> repuestosSimulados = new ArrayList<>();
    repuestosSimulados.add(new Repuesto("SSD 480GB", 32000.0, 3));

    when(this.servicioPresupuestoMock.obtenerRepuestosDisponibles()).thenReturn(repuestosSimulados);

    ModelAndView modelAndView = this.controladorPresupuesto.irAPresupuesto(null);

    assertThat(modelAndView.getViewName(), equalTo("presupuesto"));
    assertThat(modelAndView.getModel().get("repuestos"), notNullValue());
  }

  @Test
  public void queCalcularPresupuestoMultipleDevuelvaVistaConTotalCalculado()
    throws SinStockException, PedidoNoEncontradoException {
    PresupuestoMultipleForm form = new PresupuestoMultipleForm();
    List<ItemPresupuestoForm> items = new ArrayList<>();
    items.add(new ItemPresupuestoForm(1L, 2));
    form.setItems(items);
    Integer codigoSeguimiento = 12345;

    // ACTUALIZADO: Retiro en true y logística en 5k (esperamos que se multiplique a 10k)
    OrdenReparacion ordenMock = new OrdenReparacion();
    ordenMock.setSolicitaRetiro(true);
    ordenMock.setCostoLogistica(5000.0);
    when(this.servicioOrdenReparacionMock.consultarEstado(codigoSeguimiento)).thenReturn(ordenMock);

    when(this.servicioPresupuestoMock.calcularSubtotalRepuestos(items)).thenReturn(56000.0);
    when(this.servicioPresupuestoMock.calcularManoDeObra(56000.0)).thenReturn(20000.0);
    when(this.servicioPresupuestoMock.obtenerRepuestosDisponibles()).thenReturn(new ArrayList<>());

    ModelAndView mav =
      this.controladorPresupuesto.calcularPresupuestoMultiple(form, codigoSeguimiento);

    assertThat(mav.getViewName(), equalToIgnoringCase("presupuesto"));

    assertThat(mav.getModel().get("total"), equalTo(86000.0));
    assertThat(mav.getModel().get("codigoSeguimiento"), equalTo(codigoSeguimiento));
  }

  @Test
  public void alIrAVistaPresupuestoConCodigo_debeIncluirCodigoEnElModelo() {
    List<Repuesto> repuestosSimulados = new ArrayList<>();
    when(this.servicioPresupuestoMock.obtenerRepuestosDisponibles()).thenReturn(repuestosSimulados);
    Integer codigoSeguimiento = 12345;

    ModelAndView modelAndView = this.controladorPresupuesto.irAPresupuesto(codigoSeguimiento);

    assertThat(modelAndView.getViewName(), equalTo("presupuesto"));
    assertThat(modelAndView.getModel().get("codigoSeguimiento"), equalTo(codigoSeguimiento));
  }

  @Test
  public void alAceptarPresupuestoDeberiaCambiarEstadoYRedirigirAlHomeDelClienteConExito() {
    Integer codigo = 12345;

    com.tallerwebi.dominio.Usuario usuarioMock = mock(com.tallerwebi.dominio.Usuario.class);
    when(usuarioMock.getEmail()).thenReturn("cliente@mail.com");

    jakarta.servlet.http.HttpSession sessionMock = mock(jakarta.servlet.http.HttpSession.class);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    ModelAndView mav = controladorPresupuesto.aceptarPresupuesto(codigo, sessionMock);

    assertThat(mav.getViewName(), equalTo("redirect:/home?exito=presupuesto_aceptado"));
    verify(servicioOrdenReparacionMock, times(1)).aceptarPresupuesto(codigo, "cliente@mail.com");
  }

  @Test
  public void alRechazarPresupuestoDeberiaCambiarEstadoYRedirigirAlHomeDelClienteConExito() {
    Integer codigo = 12345;

    com.tallerwebi.dominio.Usuario usuarioMock = mock(com.tallerwebi.dominio.Usuario.class);
    when(usuarioMock.getEmail()).thenReturn("cliente@mail.com");

    jakarta.servlet.http.HttpSession sessionMock = mock(jakarta.servlet.http.HttpSession.class);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    ModelAndView mav = controladorPresupuesto.rechazarPresupuesto(codigo, sessionMock);

    assertThat(mav.getViewName(), equalTo("redirect:/home?exito=presupuesto_rechazado"));
    verify(servicioOrdenReparacionMock, times(1)).rechazarPresupuesto(codigo, "cliente@mail.com");
  }

  @Test
  public void enviarPresupuestoDeberiaInvocarAlServicioYRedirigirConExito()
    throws SinStockException, PedidoNoEncontradoException {
    PresupuestoMultipleForm form = new PresupuestoMultipleForm();
    List<ItemPresupuestoForm> items = new ArrayList<>();
    items.add(new ItemPresupuestoForm(1L, 2));
    form.setItems(items);
    Integer codigoSeguimiento = 12345;

    OrdenReparacion ordenMock = new OrdenReparacion();
    ordenMock.setSolicitaRetiro(true);
    ordenMock.setCostoLogistica(0.0);
    when(this.servicioOrdenReparacionMock.consultarEstado(codigoSeguimiento)).thenReturn(ordenMock);

    when(this.servicioPresupuestoMock.calcularSubtotalRepuestos(items)).thenReturn(56000.0);
    when(this.servicioPresupuestoMock.calcularManoDeObra(56000.0)).thenReturn(20000.0);

    ModelAndView mav = this.controladorPresupuesto.enviarPresupuesto(form, codigoSeguimiento);

    verify(this.servicioPresupuestoMock, times(1))
      .enviarPresupuesto(eq(codigoSeguimiento), eq(items), eq(76000.0));

    assertThat(mav.getViewName(), equalTo("presupuesto-enviado"));
  }

  @Test
  public void enviarPresupuestoConStockInsuficienteDeberiaCapturarExcepcionYRetornarVistaPresupuestoConMensajeDeError()
    throws SinStockException, PedidoNoEncontradoException {
    PresupuestoMultipleForm form = new PresupuestoMultipleForm();
    List<ItemPresupuestoForm> items = new ArrayList<>();
    form.setItems(items);
    Integer codigoSeguimiento = 12345;

    OrdenReparacion ordenMock = new OrdenReparacion();
    ordenMock.setSolicitaRetiro(true);
    ordenMock.setCostoLogistica(0.0);
    when(this.servicioOrdenReparacionMock.consultarEstado(codigoSeguimiento)).thenReturn(ordenMock);

    doThrow(new SinStockException("Stock insuficiente"))
      .when(this.servicioPresupuestoMock)
      .enviarPresupuesto(eq(codigoSeguimiento), eq(items), anyDouble());

    when(this.servicioPresupuestoMock.obtenerRepuestosDisponibles()).thenReturn(new ArrayList<>());

    ModelAndView mav = this.controladorPresupuesto.enviarPresupuesto(form, codigoSeguimiento);

    assertThat(mav.getViewName(), equalTo("presupuesto"));
    assertThat(mav.getModel().get("error"), equalTo("Stock insuficiente"));
    assertThat(mav.getModel().get("form"), equalTo(form));
    assertThat(mav.getModel().get("codigoSeguimiento"), equalTo(codigoSeguimiento));
  }

  @Test
  public void deberiaCrearYSetearDatosDeRepuestosMedianteItemPresupuestoFormCorrectamente() {
    ItemPresupuestoForm item = new ItemPresupuestoForm();

    item.setRepuestoId(5L);
    item.setCantidad(3);

    assertThat(item.getRepuestoId(), equalTo(5L));
    assertThat(item.getCantidad(), equalTo(3));
  }
}
