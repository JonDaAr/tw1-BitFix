package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.RepositorioOrdenReparacion;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioOrden")
public class RepositorioOrdenReparacionImpl implements RepositorioOrdenReparacion {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioOrdenReparacionImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardarOrden(OrdenReparacion orden) {
    sessionFactory.getCurrentSession().persist(orden);
  }

  @Override
  public OrdenReparacion buscarPorCodigo(Integer codigoSeguimiento) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from OrdenReparacion o " + "where o.codigoSeguimiento = :codigoSeguimiento",
        OrdenReparacion.class
      )
      .setParameter("codigoSeguimiento", codigoSeguimiento)
      .uniqueResult();
  }

  @Override
  public OrdenReparacion buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(OrdenReparacion.class, id);
  }

  @Override
  public void modificarOrden(OrdenReparacion orden) {
    sessionFactory.getCurrentSession().merge(orden);
  }

  @Override
  public long contarOrdenesActivas(Long tecnicoId) {
    Long count = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(o) " +
        "from OrdenReparacion o " +
        "where o.tecnicoAsignado.id = :tecnicoId " +
        "and o.estado <> :estadoEntregado",
        Long.class
      )
      .setParameter("tecnicoId", tecnicoId)
      .setParameter("estadoEntregado", "ENTREGADO")
      .uniqueResult();
    return count != null ? count : 0L;
  }

  @Override
  public LocalDateTime buscarFechaUltimaAsignacion(Long tecnicoId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select max(o.fechaAsignacion) " +
        "from OrdenReparacion o " +
        "where o.tecnicoAsignado.id = :tecnicoId",
        LocalDateTime.class
      )
      .setParameter("tecnicoId", tecnicoId)
      .uniqueResult();
  }

  @Override
  public List<OrdenReparacion> buscarTodas() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM OrdenReparacion", OrdenReparacion.class)
      .getResultList();
  }
}
