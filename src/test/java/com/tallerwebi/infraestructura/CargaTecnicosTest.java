package com.tallerwebi.infraestructura;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HibernateInfraestructuraTestConfig.class)
@Transactional
@Rollback
public class CargaTecnicosTest {

  @Autowired
  private SessionFactory factory;

  @Test
  public void cargaSoloCuentaOrdenesQueNoEstanFinalizadas() {
    Usuario tecnico = new Usuario();
    tecnico.setEmail("carga@test.com");
    factory.getCurrentSession().persist(tecnico);
    for (EstadoOrden estado : EstadoOrden.values()) {
      OrdenReparacion orden = new OrdenReparacion();
      orden.setEstado(estado);
      orden.setTecnicoAsignado(tecnico);
      factory.getCurrentSession().persist(orden);
    }
    long cantidad = new RepositorioOrdenReparacionImpl(factory).contarOrdenesActivas(tecnico.getId());
    assertEquals(10, cantidad);
  }
}
