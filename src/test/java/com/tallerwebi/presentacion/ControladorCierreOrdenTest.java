package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioCierreOrden;
import com.tallerwebi.dominio.excepcion.EstadoInvalidoParaCierreException;
import com.tallerwebi.dominio.excepcion.OrdenNoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorCierreOrdenTest {

  private ServicioCierreOrden servicioCierreOrdenMock;
  private ControladorCierreOrden controladorCierreOrden;

  @BeforeEach
  public void init() {
    servicioCierreOrdenMock = mock(ServicioCierreOrden.class);
    controladorCierreOrden = new ControladorCierreOrden(servicioCierreOrdenMock);
  }

  @Test
  public void test14_cerrarOrden_Exitoso_RedirigeAComprobante()
    throws OrdenNoEncontradaException, EstadoInvalidoParaCierreException {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(1L);
    when(servicioCierreOrdenMock.cerrarOrden(1L)).thenReturn(orden);

    ModelAndView modelAndView = controladorCierreOrden.cerrarOrden(1L);

    assertThat(modelAndView.getViewName(), is("redirect:/comprobante/1"));
  }

  @Test
  public void test15_cerrarOrden_OrdenNoEncontrada_MuestraVistaError()
    throws OrdenNoEncontradaException, EstadoInvalidoParaCierreException {
    when(servicioCierreOrdenMock.cerrarOrden(99L))
      .thenThrow(new OrdenNoEncontradaException("Orden inexistente"));

    ModelAndView modelAndView = controladorCierreOrden.cerrarOrden(99L);

    assertThat(modelAndView.getViewName(), is("error"));
    assertThat(modelAndView.getModel().get("error").toString(), is("Orden inexistente"));
  }

  @Test
  public void test16_cerrarOrden_EstadoInvalido_MuestraVistaError()
    throws OrdenNoEncontradaException, EstadoInvalidoParaCierreException {
    when(servicioCierreOrdenMock.cerrarOrden(2L))
      .thenThrow(new EstadoInvalidoParaCierreException("Estado inválido"));

    ModelAndView modelAndView = controladorCierreOrden.cerrarOrden(2L);

    assertThat(modelAndView.getViewName(), is("error"));
    assertThat(modelAndView.getModel().get("error").toString(), is("Estado inválido"));
  }

  @Test
  public void test17_verComprobante_Exitoso_RetornaVistaYObjetoOrdenEnModelo()
    throws OrdenNoEncontradaException {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(1L);
    orden.setNombreCliente("Carlos Rossi");

    when(servicioCierreOrdenMock.obtenerOrdenParaComprobante(1L)).thenReturn(orden);

    ModelAndView modelAndView = controladorCierreOrden.verComprobante(1L);

    assertThat(modelAndView.getViewName(), is("comprobante-entrega"));
    assertThat(modelAndView.getModel().get("orden"), is(notNullValue()));
  }

  @Test
  public void test18_verComprobante_OrdenInexistente_MuestraVistaError()
    throws OrdenNoEncontradaException {
    when(servicioCierreOrdenMock.obtenerOrdenParaComprobante(88L))
      .thenThrow(new OrdenNoEncontradaException("No encontrada"));

    ModelAndView modelAndView = controladorCierreOrden.verComprobante(88L);

    assertThat(modelAndView.getViewName(), is("error"));
  }
}
