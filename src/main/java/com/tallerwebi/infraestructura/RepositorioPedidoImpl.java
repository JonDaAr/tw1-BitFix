package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Pedido;
import com.tallerwebi.dominio.RepositorioPedido;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioPedido")
public class RepositorioPedidoImpl implements RepositorioPedido {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioPedidoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  @SuppressWarnings("PMD.CloseResource")
  public Pedido buscarPorCodigo(String codigo) {
    final Session session = sessionFactory.getCurrentSession();
    return session
      .createQuery("FROM Pedido p WHERE p.codigoSeguimiento = :codigo", Pedido.class)
      .setParameter("codigo", codigo)
      .uniqueResult();
  }
}
