package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Repuesto;
import com.tallerwebi.dominio.ServicioPresupuesto;
import com.tallerwebi.dominio.excepcion.SinStockException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorPresupuestoTest {

  private ServicioPresupuesto servicioPresupuestoMock;
  private ControladorPresupuesto controladorPresupuesto;

  @BeforeEach
  public void init() {
    this.servicioPresupuestoMock = mock(ServicioPresupuesto.class);
    this.controladorPresupuesto = new ControladorPresupuesto(this.servicioPresupuestoMock);
  }

  @Test
  public void alIrAVistaPresupuesto_debeMostrarLaVistaPresupuestoConListaDeDisponibles() {
    List<Repuesto> repuestosSimulados = new ArrayList<>();
    repuestosSimulados.add(new Repuesto("SSD 480GB", 32000.0, 3));
    when(this.servicioPresupuestoMock.obtenerRepuestosDisponibles()).thenReturn(repuestosSimulados);

    ModelAndView modelAndView = this.controladorPresupuesto.irAPresupuesto();

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
    // Dado
    PresupuestoMultipleForm form = new PresupuestoMultipleForm();
    List<ItemPresupuestoForm> items = new ArrayList<>();
    items.add(new ItemPresupuestoForm(1L, 2));
    form.setItems(items);

    when(servicioPresupuestoMock.calcularTotalPresupuesto(items)).thenReturn(76000.0);
    when(servicioPresupuestoMock.obtenerRepuestosDisponibles()).thenReturn(new ArrayList<>());

    // Cuando
    ModelAndView mav = controladorPresupuesto.calcularPresupuestoMultiple(form);

    // Entonces
    assertThat(mav.getViewName(), equalToIgnoringCase("presupuesto"));
    assertThat(mav.getModel().get("total"), equalTo(76000.0));
  }
}
