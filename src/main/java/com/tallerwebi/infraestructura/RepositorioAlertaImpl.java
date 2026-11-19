package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Alerta;
import com.tallerwebi.dominio.RepositorioAlerta;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioAlertaImpl implements RepositorioAlerta {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioAlertaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public boolean existeAlertaPendienteConMensaje(String mensaje) {
    String sql = "SELECT COUNT(a) FROM Alerta a WHERE a.mensaje = :mensaje";
    Long count = sessionFactory
      .getCurrentSession()
      .createQuery(sql, Long.class)
      .setParameter("mensaje", mensaje)
      .uniqueResult();
    return count > 0;
  }

  @Override
  public void guardar(Alerta alerta) {
    sessionFactory.getCurrentSession().save(alerta);
  }

  @Override
  public List<Alerta> obtenerAlertasPendientes() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM Alerta ORDER BY fechaCreacion DESC", Alerta.class)
      .setMaxResults(30) // Traemos como máximo las últimas 30 pendientes
      .getResultList();
  }

  @Override
  public Alerta buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Alerta.class, id);
  }

  @Override
  public void borrar(Alerta alerta) {
    sessionFactory.getCurrentSession().delete(alerta);
  }

  @Override
  public void eliminarPorUrlAccion(String urlAccion) {
    Alerta alerta = sessionFactory
      .getCurrentSession()
      .createQuery("FROM Alerta WHERE urlAccion = :url", Alerta.class)
      .setParameter("url", urlAccion)
      .uniqueResult();

    if (alerta != null) {
      sessionFactory.getCurrentSession().delete(alerta);
    }
  }
}
