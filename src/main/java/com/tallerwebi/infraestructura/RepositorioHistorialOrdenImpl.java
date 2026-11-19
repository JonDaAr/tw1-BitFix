package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.HistorialOrden;
import com.tallerwebi.dominio.RepositorioHistorialOrden;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioHistorialOrdenImpl implements RepositorioHistorialOrden {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioHistorialOrdenImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(HistorialOrden historial) {
    sessionFactory.getCurrentSession().persist(historial);
  }

  @Override
  public List<HistorialOrden> buscarPorOrden(Long ordenId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from HistorialOrden h where h.ordenReparacion.id = :ordenId order by h.fechaHora asc",
        HistorialOrden.class
      )
      .setParameter("ordenId", ordenId)
      .getResultList();
  }
}
