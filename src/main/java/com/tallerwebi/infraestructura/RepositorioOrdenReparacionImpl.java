package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.RepositorioOrdenReparacion;
import com.tallerwebi.dominio.Usuario;
import java.util.List;
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
  public OrdenReparacion buscarOrdenPorCodigo(Integer codigoSeguimiento) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from OrdenReparacion where codigoSeguimiento = :codigoSeguimiento",
        OrdenReparacion.class
      )
      .setParameter("codigoSeguimiento", codigoSeguimiento)
      .uniqueResult();
  }

  //------------

  @Override
  public OrdenReparacion buscarPorIdOrdenReparacion(Long idOrdenReparacion) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from OrdenReparacion where idOrdenReparacion = :idOrdenReparacion",
        OrdenReparacion.class
      )
      .setParameter("idOrdenReparacion", idOrdenReparacion)
      .uniqueResult();
  }

  @Override
  public List<OrdenReparacion> listarTodasLasOrdenes() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from OrdenReparacion", OrdenReparacion.class)
      .getResultList();
  }
}
