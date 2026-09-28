package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.RepositorioOrdenReparacion;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioOrdenReparacionTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioOrdenReparacion repositorioOrdenReparacion;

  @BeforeEach
  public void init() {
    repositorioOrdenReparacion = new RepositorioOrdenReparacionImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void queSePuedaGuardarUnaOrdenReparacion() {
    OrdenReparacion nuevaOrdenReparacion = new OrdenReparacion("Test", "12345678", "Test", "Test");

    this.repositorioOrdenReparacion.guardarOrden(nuevaOrdenReparacion);

    assertNotNull(nuevaOrdenReparacion.getIdOrdenReparacion());
  }

  @Test
  @Transactional
  @Rollback
  public void queSePuedaGuardarUnaOrdenReparacionConCodigoDeSeguimientoCorrespondienteALaMismaOrden() {
    OrdenReparacion nuevaOrdenReparacion = new OrdenReparacion("Test", "12345678", "Test", "Test");
    Integer codigoEsperado = nuevaOrdenReparacion.generarCodigoSeguimientoUnico();

    this.repositorioOrdenReparacion.guardarOrden(nuevaOrdenReparacion);

    OrdenReparacion ordenGuardada =
      this.sessionFactory.getCurrentSession()
        .get(OrdenReparacion.class, nuevaOrdenReparacion.getIdOrdenReparacion());

    assertEquals(codigoEsperado, ordenGuardada.getCodigoSeguimiento());
  }

  @Test
  @Transactional
  @Rollback
  public void queSePuedaBuscarUnaOrdenReparacionConElCodigoDeSeguimiento() {
    // preparacion
    OrdenReparacion nuevaOrdenReparacion = new OrdenReparacion("Test", "12345678", "Test", "Test");
    Integer codigoGenerado = nuevaOrdenReparacion.generarCodigoSeguimientoUnico();

    this.sessionFactory.getCurrentSession().persist(nuevaOrdenReparacion);

    // ejecucion
    OrdenReparacion ordenEncontrada =
      this.repositorioOrdenReparacion.buscarPorCodigo(codigoGenerado);

    // validacion
    assertThat(ordenEncontrada, is(notNullValue()));
    assertThat(ordenEncontrada.getCodigoSeguimiento(), is(equalTo(codigoGenerado)));
  }
}
