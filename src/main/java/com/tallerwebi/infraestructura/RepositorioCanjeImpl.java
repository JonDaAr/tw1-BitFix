package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.ComputadoraUsada;
import com.tallerwebi.dominio.RepositorioCanje;
import com.tallerwebi.dominio.SolicitudCanje;
import java.util.List;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioCanjeImpl implements RepositorioCanje {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioCanjeImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardarComputadora(ComputadoraUsada computadora) {
    sessionFactory.getCurrentSession().saveOrUpdate(computadora);
  }

  @Override
  public ComputadoraUsada buscarComputadora(Long id) {
    return sessionFactory.getCurrentSession().get(ComputadoraUsada.class, id);
  }

  @Override
  public ComputadoraUsada buscarComputadoraConBloqueo(Long id) {
    return sessionFactory
      .getCurrentSession()
      .get(ComputadoraUsada.class, id, LockMode.PESSIMISTIC_WRITE);
  }

  @Override
  public List<ComputadoraUsada> listarComputadorasDisponibles() {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from ComputadoraUsada where activa = true and stock > 0 and (retirada = false or retirada is null) order by id desc",
        ComputadoraUsada.class
      )
      .getResultList();
  }

  @Override
  public List<ComputadoraUsada> listarComputadoras() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from ComputadoraUsada order by id desc", ComputadoraUsada.class)
      .getResultList();
  }

  @Override
  public void guardarSolicitud(SolicitudCanje solicitud) {
    sessionFactory.getCurrentSession().persist(solicitud);
  }

  @Override
  public SolicitudCanje buscarSolicitud(Long id) {
    return sessionFactory.getCurrentSession().get(SolicitudCanje.class, id);
  }

  @Override
  public List<SolicitudCanje> listarSolicitudes() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from SolicitudCanje order by fechaSolicitud desc", SolicitudCanje.class)
      .getResultList();
  }

  @Override
  public List<SolicitudCanje> listarSolicitudesDeCliente(Long clienteId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from SolicitudCanje where cliente.id = :clienteId order by fechaSolicitud desc",
        SolicitudCanje.class
      )
      .setParameter("clienteId", clienteId)
      .getResultList();
  }
}
