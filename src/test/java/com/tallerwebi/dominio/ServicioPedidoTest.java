package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioPedidoTest {

  private RepositorioPedido repositorioPedidoMock;
  private ServicioPedido servicioPedido;

  @BeforeEach
  public void init() {
    repositorioPedidoMock = mock(RepositorioPedido.class);
    servicioPedido = new ServicioPedidoImpl(repositorioPedidoMock);
  }

  @Test
  public void queDevuelvaElPedidoSiElCodigoExiste() throws PedidoNoEncontradoException {
    // // preparacionv
    String codigo = "REC-100";
    Pedido pedidoEsperado = new Pedido(codigo, "RECIBIDO");
    when(repositorioPedidoMock.buscarPorCodigo(codigo)).thenReturn(pedidoEsperado);

    // ejecucion
    Pedido pedidoObtenido = servicioPedido.consultarEstado(codigo);

    // validacion
    assertThat(pedidoObtenido, is(notNullValue()));
    assertThat(pedidoObtenido.getCodigoSeguimiento(), is(equalTo(codigo)));
    verify(repositorioPedidoMock, times(1)).buscarPorCodigo(codigo);
  }

  @Test
  public void queLanceExcepcionSiElCodigoNoExiste() {
    // preparacion
    String codigoInexistente = "INVALIDO";
    when(repositorioPedidoMock.buscarPorCodigo(codigoInexistente)).thenReturn(null);

    // ejecucion // validacion
    assertThrows(
      PedidoNoEncontradoException.class,
      () -> {
        servicioPedido.consultarEstado(codigoInexistente);
      }
    );
  }

  @Test
  public void queLanceExcepcionSiElCodigoEsVacio() {
    // ejecucion // validacion
    assertThrows(
      PedidoNoEncontradoException.class,
      () -> {
        servicioPedido.consultarEstado("");
      }
    );
  }
}
