package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tallerwebi.dominio.EstadoOrden;
import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.RepositorioOrdenReparacion;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

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

  @Test
  @Transactional
  @Rollback
  public void queSePuedaBuscarPorIdYModificarEstadoDeUnaOrden() {
    OrdenReparacion orden = new OrdenReparacion("Carlos", "11223344", "PC", "Falla disco");
    orden.setEstado(EstadoOrden.REPARADO);
    this.repositorioOrdenReparacion.guardarOrden(orden);

    orden.setEstado(EstadoOrden.ENTREGADO);
    this.repositorioOrdenReparacion.modificarOrden(orden);

    OrdenReparacion modificada =
      this.repositorioOrdenReparacion.buscarPorId(orden.getIdOrdenReparacion());
    assertThat(modificada, is(notNullValue()));
    assertThat(modificada.getEstado(), is(EstadoOrden.ENTREGADO));
  }

  @Test
  @Transactional
  @Rollback
  public void queAlBuscarPorIdInexistenteRetorneNull() {
    OrdenReparacion orden = this.repositorioOrdenReparacion.buscarPorId(9999L);
    assertThat(orden, is(nullValue()));
  }

  //-----test gestion estados y diagnostico(nota tecnica)
  @Test
  @Transactional
  @Rollback
  public void queSePuedaObtenerUnaOrdenPorIdOrdenReparacion() {/*ES DIFERENTE A CODIGO DE SEGUIMIENTO*/
    OrdenReparacion nuevaOrdenReparacion = new OrdenReparacion("Test", "12345678", "Test", "Test");
    this.repositorioOrdenReparacion.guardarOrden(nuevaOrdenReparacion);

    OrdenReparacion encontrada =
            this.repositorioOrdenReparacion.buscarPorId(
                    nuevaOrdenReparacion.getIdOrdenReparacion()
            );
    assertNotNull(encontrada);
    assertEquals(nuevaOrdenReparacion, encontrada);
    assertEquals(nuevaOrdenReparacion.getIdOrdenReparacion(), encontrada.getIdOrdenReparacion());
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueSeRegistranTresOrdenesSeObtenganTodasEnUnaLista() {
    OrdenReparacion nuevaOrdenReparacion1 = new OrdenReparacion("Test", "12345678", "Test", "Test");
    OrdenReparacion nuevaOrdenReparacion2 = new OrdenReparacion("Test", "12345678", "Test", "Test");
    OrdenReparacion nuevaOrdenReparacion3 = new OrdenReparacion("Test", "12345678", "Test", "Test");

    this.repositorioOrdenReparacion.guardarOrden(nuevaOrdenReparacion1);
    this.repositorioOrdenReparacion.guardarOrden(nuevaOrdenReparacion2);
    this.repositorioOrdenReparacion.guardarOrden(nuevaOrdenReparacion3);

    List<OrdenReparacion> ordenes = this.repositorioOrdenReparacion.listarTodasLasOrdenes();
    assertEquals(3, ordenes.size());
    assertNotNull(ordenes.get(2).getIdOrdenReparacion());
    assertEquals(
            ordenes.get(0).getIdOrdenReparacion(),
            nuevaOrdenReparacion1.getIdOrdenReparacion()
    );
  }

}
