package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioRepuesto;
import com.tallerwebi.dominio.Repuesto;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioRepuestoImpl implements RepositorioRepuesto {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioRepuestoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Repuesto repuesto) {}

  @Override
  public Repuesto buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Repuesto.class, id);
  }

  @Override
  public List<Repuesto> obtenerTodos() {
    return List.of();
  }

  @Override
  public List<Repuesto> obtenerDisponibles() {
    return List.of();
  }

  @Override
  public List<Repuesto> obtenerRepuestosDisponibles() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM Repuesto WHERE stock > 0", Repuesto.class)
      .list();
  }
}
