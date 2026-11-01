package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.Repuesto;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.ServicioPresupuesto;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.dominio.excepcion.SinStockException;
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

  @BeforeEach
  public void init() {
    this.servicioPresupuestoMock = mock(ServicioPresupuesto.class);
    this.servicioOrdenReparacionMock = mock(ServicioOrdenReparacion.class);
    this.controladorPresupuesto =
      new ControladorPresupuesto(this.servicioPresupuestoMock, this.servicioOrdenReparacionMock);
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

    // 3. Verificamos que el servicio se llame con ambos parámetros
    verify(this.servicioOrdenReparacionMock, times(1)).aceptarPresupuesto(codigoSeguimiento, email);
    assertThat(mav.getViewName(), equalTo("redirect:/consulta-estado?codigoSeguimiento=123456"));
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
    assertThat(mav.getViewName(), equalTo("redirect:/consulta-estado?codigoSeguimiento=123456"));
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
    throws SinStockException {
    PresupuestoMultipleForm form = new PresupuestoMultipleForm();
    List<ItemPresupuestoForm> items = new ArrayList<>();
    items.add(new ItemPresupuestoForm(1L, 2));
    form.setItems(items);
    Integer codigoSeguimiento = 12345;

    when(this.servicioPresupuestoMock.calcularSubtotalRepuestos(items)).thenReturn(56000.0);
    when(this.servicioPresupuestoMock.calcularManoDeObra(56000.0)).thenReturn(20000.0);
    when(this.servicioPresupuestoMock.obtenerRepuestosDisponibles()).thenReturn(new ArrayList<>());

    ModelAndView mav =
      this.controladorPresupuesto.calcularPresupuestoMultiple(form, codigoSeguimiento);

    assertThat(mav.getViewName(), equalToIgnoringCase("presupuesto"));
    assertThat(mav.getModel().get("total"), equalTo(76000.0));
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
}
