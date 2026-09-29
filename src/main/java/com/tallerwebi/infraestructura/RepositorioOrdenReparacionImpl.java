package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.RepositorioOrdenReparacion;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioOrdenReparacion")
public class RepositorioOrdenReparacionImpl implements RepositorioOrdenReparacion {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioOrdenReparacionImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardarOrden(OrdenReparacion nuevaOrdenReparacion) {
    sessionFactory.getCurrentSession().persist(nuevaOrdenReparacion);
  }

  @Override
  public OrdenReparacion buscarPorCodigo(Integer codigo) {
      return sessionFactory
              .getCurrentSession()
              .createQuery(
                      "from OrdenReparacion o where o.codigoSeguimiento = :codigo",
                      OrdenReparacion.class
              )
              .setParameter("codigo", codigo)
              .uniqueResult();
  }
  public OrdenReparacion buscarOrdenPorCodigo(Integer codigoSeguimiento) {
    CriteriaBuilder builder = sessionFactory.getCurrentSession().getCriteriaBuilder();
    CriteriaQuery<OrdenReparacion> criteria = builder.createQuery(OrdenReparacion.class);
    Root<OrdenReparacion> root = criteria.from(OrdenReparacion.class);

    criteria.select(root).where(builder.equal(root.get("codigoSeguimiento"), codigoSeguimiento));

    TypedQuery<OrdenReparacion> query = sessionFactory.getCurrentSession().createQuery(criteria);
    return query.getResultList().stream().findFirst().orElse(null);
  }

  @Override
  public OrdenReparacion buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(OrdenReparacion.class, id);
  }

  @Override
  public void modificarOrden(OrdenReparacion orden) {
    sessionFactory.getCurrentSession().merge(orden);
  }
}
