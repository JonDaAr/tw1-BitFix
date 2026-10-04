package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Repuesto;
import com.tallerwebi.dominio.ServicioInventario;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorInventarioTest {

  private ServicioInventario servicioInventarioMock;
  private ControladorInventario controladorInventario;

  @BeforeEach
  public void init() {
    servicioInventarioMock = mock(ServicioInventario.class);
    controladorInventario = new ControladorInventario(servicioInventarioMock);
  }

  @Test
  public void queAlVerInventarioDevuelvaLaVistaConListadoYFormularioNuevo() {
    // Dado
    List<Repuesto> repuestos = new ArrayList<>();
    Integer codigoSeguimiento = 100001;

    when(servicioInventarioMock.listarTodos()).thenReturn(repuestos);

    // Cuando
    ModelAndView mav = controladorInventario.verInventario(codigoSeguimiento);

    // Entonces
    assertThat(mav.getViewName(), equalToIgnoringCase("inventario"));
    assertThat(mav.getModel().get("repuestos"), notNullValue());
    assertThat(mav.getModel().get("nuevoRepuesto"), notNullValue());
    assertThat(mav.getModel().get("codigoSeguimiento"), equalTo(codigoSeguimiento));
  }

  @Test
  public void queAlGuardarRepuestoInvoqueAlServicioYRedirijaAInventario() {
    // Dado
    Repuesto repuesto = new Repuesto();
    repuesto.setNombre("SSD NVMe 500GB");

    Integer codigoSeguimiento = 100001;

    // Cuando
    ModelAndView mav = controladorInventario.guardarRepuesto(repuesto, codigoSeguimiento);

    // Entonces
    verify(servicioInventarioMock, times(1)).guardarOActualizar(repuesto);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/inventario?codigo=100001"));
  }

  @Test
  public void queAlEditarRepuestoRetorneLaVistaConElObjetoCargado() {
    // Dado
    Long repuestoId = 10L;
    Integer codigoSeguimiento = 100001;
    Repuesto repuesto = new Repuesto();
    repuesto.setId(repuestoId);

    when(servicioInventarioMock.buscarPorId(repuestoId)).thenReturn(repuesto);
    when(servicioInventarioMock.listarTodos()).thenReturn(new ArrayList<>());

    // Cuando
    ModelAndView mav = controladorInventario.editarRepuesto(repuestoId, codigoSeguimiento);

    // Entonces
    assertThat(mav.getViewName(), equalToIgnoringCase("inventario"));
    assertThat(mav.getModel().get("nuevoRepuesto"), notNullValue());
    assertThat(mav.getModel().get("codigoSeguimiento"), equalTo(codigoSeguimiento));
    verify(servicioInventarioMock, times(1)).buscarPorId(repuestoId);
  }

  @Test
  public void queAlEliminarRepuestoInvoqueAlServicioYRedirijaAInventario() {
    // Dado
    Long repuestoId = 5L;

    // Cuando
    ModelAndView mav = controladorInventario.eliminarRepuesto(repuestoId);

    // Entonces
    verify(servicioInventarioMock, times(1)).eliminar(repuestoId);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/inventario"));
  }
}
