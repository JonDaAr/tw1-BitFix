package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Pedido;
import com.tallerwebi.dominio.ServicioPedido;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControllerConsultaPublicaTest {

  private ServicioPedido servicioPedidoMock;
  private ControllerConsultaPublica controller;

  @BeforeEach
  public void init() {
    servicioPedidoMock = mock(ServicioPedido.class);
    controller = new ControllerConsultaPublica(servicioPedidoMock);
  }

  @Test
  public void queMuestreLaVistaDeBusquedaAlNavegarAConsultarEstado() {
    ModelAndView mav = controller.irAConsulta();

    assertThat(mav.getViewName(), is(equalTo("consultar-estado")));
    assertThat(mav.getModel().get("datosConsulta"), is(notNullValue()));
  }

  @Test
  public void queRetorneLaVistaResultadoSiElPedidoExiste() throws PedidoNoEncontradoException {
    // preparacion
    DatosConsultaEstado datos = new DatosConsultaEstado();
    datos.setCodigoSeguimiento("REC-100");

    Pedido pedidoFake = new Pedido("REC-100", "EN_DIAGNOSTICO");
    when(servicioPedidoMock.consultarEstado("REC-100")).thenReturn(pedidoFake);

    // ejecucion
    ModelAndView mav = controller.buscarEstado(datos);

    // validacion
    assertThat(mav.getViewName(), is(equalTo("resultado-consulta")));
    assertThat(mav.getModel().get("pedido"), is(equalTo(pedidoFake)));
  }

  @Test
  public void queMuestreMensajeDeErrorSiElPedidoNoExiste() throws PedidoNoEncontradoException {
    // preparacion
    DatosConsultaEstado datos = new DatosConsultaEstado();
    datos.setCodigoSeguimiento("INVALIDO");

    when(servicioPedidoMock.consultarEstado("INVALIDO"))
      .thenThrow(new PedidoNoEncontradoException("No se encontró ningún pedido"));

    // ejecucion
    ModelAndView mav = controller.buscarEstado(datos);

    // validacion
    assertThat(mav.getViewName(), is(equalTo("consultar-estado")));
    assertThat(mav.getModel().get("error"), is(equalTo("No se encontró ningún pedido")));
  }
}
