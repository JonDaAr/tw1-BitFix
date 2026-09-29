package com.tallerwebi.infraestructura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.RepositorioOrdenReparacion;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
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
    OrdenReparacion nuevaOrdenReparacion = new OrdenReparacion("Test", "12345678", "Test", "Test");
    Integer codigoGenerado = nuevaOrdenReparacion.generarCodigoSeguimientoUnico();

    this.repositorioOrdenReparacion.guardarOrden(nuevaOrdenReparacion);
    OrdenReparacion ordenEncontrada =
      this.repositorioOrdenReparacion.buscarOrdenPorCodigo(codigoGenerado);

    assertEquals(nuevaOrdenReparacion, ordenEncontrada);
  }

  //------------

  @Test
  @Transactional
  @Rollback
  public void queSePuedaObtenerUnaOrdenPorIdOrdenReparacion() {/*ES DIFERENTE A CODIGO DE SEGUIMIENTO*/
    OrdenReparacion nuevaOrdenReparacion = new OrdenReparacion("Test", "12345678", "Test", "Test");
    this.repositorioOrdenReparacion.guardarOrden(nuevaOrdenReparacion);

    OrdenReparacion encontrada =
      this.repositorioOrdenReparacion.buscarPorIdOrdenReparacion(
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
