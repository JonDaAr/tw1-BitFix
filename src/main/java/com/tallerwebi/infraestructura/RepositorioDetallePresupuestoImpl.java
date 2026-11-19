package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.DetallePresupuesto;
import com.tallerwebi.dominio.RepositorioDetallePresupuesto;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioDetallePresupuestoImpl implements RepositorioDetallePresupuesto {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioDetallePresupuestoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(DetallePresupuesto detalle) {
    sessionFactory.getCurrentSession().save(detalle);
  }

  @Override
  public void modificar(DetallePresupuesto detalle) {
    sessionFactory.getCurrentSession().merge(detalle);
  }

  @Override
  public List<DetallePresupuesto> buscarPorOrden(Long ordenId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from DetallePresupuesto d where d.ordenReparacion.idOrdenReparacion = :ordenId",
        DetallePresupuesto.class
      )
      .setParameter("ordenId", ordenId)
      .getResultList();
  }
}
