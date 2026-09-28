package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
    OrdenReparacion nuevaOrdenReparacion = new OrdenReparacion("Test", "12345678", "Test", "Test");
    Integer codigoGenerado = nuevaOrdenReparacion.generarCodigoSeguimientoUnico();

    this.repositorioOrdenReparacion.guardarOrden(nuevaOrdenReparacion);
    OrdenReparacion ordenEncontrada =
      this.repositorioOrdenReparacion.buscarOrdenPorCodigo(codigoGenerado);

    assertEquals(nuevaOrdenReparacion, ordenEncontrada);
  }

  @Test
  @Transactional
  @Rollback
  public void queSePuedaBuscarPorIdYModificarEstadoDeUnaOrden() {
    OrdenReparacion orden = new OrdenReparacion("Carlos", "11223344", "PC", "Falla disco");
    orden.setEstado("REPARADO");
    this.repositorioOrdenReparacion.guardarOrden(orden);

    orden.setEstado("ENTREGADO");
    this.repositorioOrdenReparacion.modificarOrden(orden);

    OrdenReparacion modificada =
      this.repositorioOrdenReparacion.buscarPorId(orden.getIdOrdenReparacion());
    assertThat(modificada, is(notNullValue()));
    assertThat(modificada.getEstado(), is("ENTREGADO"));
  }

  @Test
  @Transactional
  @Rollback
  public void queAlBuscarPorIdInexistenteRetorneNull() {
    OrdenReparacion orden = this.repositorioOrdenReparacion.buscarPorId(9999L);
    assertThat(orden, is(nullValue()));
  }
}
