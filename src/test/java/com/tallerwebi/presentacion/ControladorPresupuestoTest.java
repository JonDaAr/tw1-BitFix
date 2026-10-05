package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Repuesto;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.ServicioPresupuesto;
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

    ModelAndView mav = this.controladorPresupuesto.aceptarPresupuesto(codigoSeguimiento);

    verify(this.servicioOrdenReparacionMock, times(1)).aceptarPresupuesto(codigoSeguimiento);
    assertThat(mav.getViewName(), equalTo("redirect:/consulta-estado?codigoSeguimiento=123456"));
  }

  @Test
  public void alAceptarPresupuestoInexistente_debeMostrarError() {
    Integer codigoSeguimiento = 999999;
    doThrow(new IllegalArgumentException("No se encontró la orden"))
      .when(this.servicioOrdenReparacionMock)
      .aceptarPresupuesto(codigoSeguimiento);

    ModelAndView mav = this.controladorPresupuesto.aceptarPresupuesto(codigoSeguimiento);

    assertThat(mav.getViewName(), equalTo("consultar-estado"));
    assertThat(mav.getModel().get("error"), equalTo("No se encontró la orden"));
  }

  @Test
  public void alRechazarPresupuesto_debeInvocarAlServicioYRedirigirAConsulta() {
    Integer codigoSeguimiento = 123456;

    ModelAndView mav = this.controladorPresupuesto.rechazarPresupuesto(codigoSeguimiento);

    verify(this.servicioOrdenReparacionMock, times(1)).rechazarPresupuesto(codigoSeguimiento);
    assertThat(mav.getViewName(), equalTo("redirect:/consulta-estado?codigoSeguimiento=123456"));
  }

  @Test
  public void alRechazarPresupuestoInexistente_debeMostrarError() {
    Integer codigoSeguimiento = 999999;
    doThrow(new IllegalArgumentException("No se encontró la orden"))
      .when(this.servicioOrdenReparacionMock)
      .rechazarPresupuesto(codigoSeguimiento);

    ModelAndView mav = this.controladorPresupuesto.rechazarPresupuesto(codigoSeguimiento);

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
  public void cuandoSeCalculaConStockValido_debeMostrarSubtotalCalculado()
    throws SinStockException {
    when(this.servicioPresupuestoMock.calcularSubtotalRepuesto(1L, 2)).thenReturn(64000.0);

    ModelAndView modelAndView = this.controladorPresupuesto.calcularSubtotal(1L, 2);

    assertThat(modelAndView.getViewName(), equalTo("presupuesto"));
    assertThat(modelAndView.getModel().get("subtotal"), equalTo(64000.0));
  }

  @Test
  public void cuandoNoHayStockSuficiente_debeMostrarMensajeDeError() throws SinStockException {
    doThrow(new SinStockException("Stock no disponible"))
      .when(this.servicioPresupuestoMock)
      .calcularSubtotalRepuesto(1L, 10);

    ModelAndView modelAndView = this.controladorPresupuesto.calcularSubtotal(1L, 10);

    assertThat(modelAndView.getViewName(), equalTo("presupuesto"));
    assertThat(modelAndView.getModel().get("error"), equalTo("Stock no disponible"));
  }

  @Test
  public void queCalcularPresupuestoMultipleDevuelvaVistaConTotalCalculado()
    throws SinStockException {
    PresupuestoMultipleForm form = new PresupuestoMultipleForm();
    List<ItemPresupuestoForm> items = new ArrayList<>();
    items.add(new ItemPresupuestoForm(1L, 2));
    form.setItems(items);
    Integer codigoSeguimiento = 12345;

    when(this.servicioPresupuestoMock.calcularTotalPresupuesto(items)).thenReturn(76000.0);
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
