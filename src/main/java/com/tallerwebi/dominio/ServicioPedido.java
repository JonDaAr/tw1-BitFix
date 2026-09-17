package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;

public abstract class ServicioPedido {

  public abstract Pedido consultarEstado(String codigo) throws PedidoNoEncontradoException;
}
