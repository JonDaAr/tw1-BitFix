package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;

public interface ServicioPedido {
  Pedido consultarEstado(String codigo) throws PedidoNoEncontradoException;
}
