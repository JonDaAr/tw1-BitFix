package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioSucursal;
import com.tallerwebi.dominio.Sucursal;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioSucursalImpl implements RepositorioSucursal {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioSucursalImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<Sucursal> obtenerTodas() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM Sucursal", Sucursal.class)
      .getResultList();
  }
}
