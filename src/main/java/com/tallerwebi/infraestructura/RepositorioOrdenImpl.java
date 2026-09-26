package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Orden;
import com.tallerwebi.dominio.RepositorioOrden;
import java.time.LocalDateTime;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioOrden")
public class RepositorioOrdenImpl implements RepositorioOrden {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioOrdenImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Orden orden) {
    sessionFactory.getCurrentSession().persist(orden);
  }

  @Override
  public long contarOrdenesActivas(Long tecnicoId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(o) " +
        "from Orden o " +
        "where o.tecnicoAsignado.id = :tecnicoId " +
        "and o.estado <> :estadoEntregado",
        Long.class
      )
      .setParameter("tecnicoId", tecnicoId)
      .setParameter("estadoEntregado", "ENTREGADO")
      .getSingleResult();
  }

  @Override
  public LocalDateTime buscarFechaUltimaAsignacion(Long tecnicoId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select max(o.fechaAsignacion) " +
        "from Orden o " +
        "where o.tecnicoAsignado.id = :tecnicoId",
        LocalDateTime.class
      )
      .setParameter("tecnicoId", tecnicoId)
      .getSingleResult();
  }
}
