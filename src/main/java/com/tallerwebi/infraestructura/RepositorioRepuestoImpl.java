package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioRepuesto;
import com.tallerwebi.dominio.Repuesto;
import java.util.List;
import org.hibernate.LockMode; // IMPORTANTE: Importamos el modo de bloqueo
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
  public void guardar(Repuesto repuesto) {
    this.sessionFactory.getCurrentSession().saveOrUpdate(repuesto);
  }

  @Override
  public Repuesto buscarPorId(Long id) {
    if (id == null) {
      return null;
    }
    return this.sessionFactory.getCurrentSession().get(Repuesto.class, id);
  }

  // NUEVO MÉTODO: Bloquea la fila en la BD para evitar condiciones de carrera
  @Override
  public Repuesto buscarPorIdConBloqueo(Long id) {
    if (id == null) {
      return null;
    }
    return this.sessionFactory.getCurrentSession().get(Repuesto.class, id, LockMode.PESSIMISTIC_WRITE);
  }

  @Override
  public List<Repuesto> obtenerTodos() {
    return this.sessionFactory.getCurrentSession()
            .createQuery("FROM Repuesto", Repuesto.class)
            .list();
  }

  @Override
  public List<Repuesto> obtenerDisponibles() {
    return this.sessionFactory.getCurrentSession()
            .createQuery("FROM Repuesto WHERE stock > 0", Repuesto.class)
            .list();
  }

  @Override
  public void eliminar(Repuesto repuesto) {
    this.sessionFactory.getCurrentSession().delete(repuesto);
  }
}