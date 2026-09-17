package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioPedido")
@Transactional
public class ServicioPedidoImpl extends ServicioPedido {

  private final RepositorioPedido repositorioPedido;

  @Autowired
  public ServicioPedidoImpl(RepositorioPedido repositorioPedido) {
    this.repositorioPedido = repositorioPedido;
  }

  @Override
  public Pedido consultarEstado(String codigo) throws PedidoNoEncontradoException {
    if (codigo == null || codigo.trim().isEmpty()) {
      throw new PedidoNoEncontradoException("El código no puede estar vacío");
    }

    Pedido pedido = repositorioPedido.buscarPorCodigo(codigo);
    if (pedido == null) {
      throw new PedidoNoEncontradoException("No se encontró ningún pedido con el código ingresado");
    }

    return pedido;
  }
}
