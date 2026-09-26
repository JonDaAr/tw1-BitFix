package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.Orden;
import com.tallerwebi.dominio.RepositorioOrden;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioOrdenTest {

  @Autowired
  private org.hibernate.SessionFactory sessionFactory;

  private RepositorioOrden repositorioOrden;

  @BeforeEach
  public void init() {
    repositorioOrden = new RepositorioOrdenImpl(sessionFactory);
  }

  @Test
  @Transactional
  public void deberiaGuardarUnaOrdenConTecnicoAsignado() {
    Usuario tecnico = crearTecnico("tecnico@test.com");
    sessionFactory.getCurrentSession().persist(tecnico);

    Orden orden = new Orden();
    orden.setTipoEquipo("Notebook Dell");
    orden.setTecnicoAsignado(tecnico);
    orden.setFechaAsignacion(LocalDateTime.now());

    repositorioOrden.guardar(orden);
    sessionFactory.getCurrentSession().flush();

    Orden obtenido = sessionFactory.getCurrentSession().find(Orden.class, orden.getId());
    assertThat(obtenido.getTecnicoAsignado().getId(), equalTo(tecnico.getId()));
  }

  @Test
  @Transactional
  public void deberiaContarSoloOrdenesNoEntregadas() {
    Usuario tecnico = crearTecnico("tecnico@test.com");
    sessionFactory.getCurrentSession().persist(tecnico);
    persistirOrden(tecnico, "RECIBIDO");
    persistirOrden(tecnico, "EN_REPARACION");
    persistirOrden(tecnico, "ENTREGADO");
    sessionFactory.getCurrentSession().flush();

    long cantidad = repositorioOrden.contarOrdenesActivas(tecnico.getId());

    assertThat(cantidad, equalTo(2L));
  }

  @Test
  @Transactional
  public void deberiaRetornarLaUltimaFechaDeAsignacion() {
    Usuario tecnico = crearTecnico("tecnico@test.com");
    sessionFactory.getCurrentSession().persist(tecnico);
    LocalDateTime antigua = LocalDateTime.of(2026, 9, 1, 10, 0);
    LocalDateTime reciente = LocalDateTime.of(2026, 9, 10, 10, 0);
    persistirOrden(tecnico, "RECIBIDO", antigua);
    persistirOrden(tecnico, "ENTREGADO", reciente);
    sessionFactory.getCurrentSession().flush();

    LocalDateTime resultado = repositorioOrden.buscarFechaUltimaAsignacion(tecnico.getId());

    assertThat(resultado, equalTo(reciente));
  }

  @Test
  @Transactional
  public void deberiaRetornarNullSiElTecnicoNuncaRecibioUnaOrden() {
    Usuario tecnico = crearTecnico("tecnico@test.com");
    sessionFactory.getCurrentSession().persist(tecnico);
    sessionFactory.getCurrentSession().flush();

    LocalDateTime resultado = repositorioOrden.buscarFechaUltimaAsignacion(tecnico.getId());

    assertThat(resultado, nullValue());
  }

  private Usuario crearTecnico(String email) {
    Usuario tecnico = new Usuario();
    tecnico.setEmail(email);
    tecnico.setPassword("1234");
    tecnico.setRol("TECNICO");
    tecnico.setActivo(true);
    return tecnico;
  }

  private void persistirOrden(Usuario tecnico, String estado) {
    persistirOrden(tecnico, estado, LocalDateTime.now());
  }

  private void persistirOrden(Usuario tecnico, String estado, LocalDateTime fechaAsignacion) {
    Orden orden = new Orden();
    orden.setTecnicoAsignado(tecnico);
    orden.setEstado(estado);
    orden.setFechaAsignacion(fechaAsignacion);
    sessionFactory.getCurrentSession().persist(orden);
  }
}
