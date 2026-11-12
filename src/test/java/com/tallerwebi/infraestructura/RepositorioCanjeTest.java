package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tallerwebi.dominio.ComputadoraUsada;
import com.tallerwebi.dominio.EstadoSolicitudCanje;
import com.tallerwebi.dominio.SolicitudCanje;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class RepositorioCanjeTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioCanjeImpl repositorio;

  @BeforeEach
  public void init() {
    repositorio = new RepositorioCanjeImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void computadoraSeGuardaBuscaYBloqueaParaTasacion() {
    ComputadoraUsada computadora = computadora("Dell", "Latitude", true, 2);
    repositorio.guardarComputadora(computadora);

    assertThat(repositorio.buscarComputadora(computadora.getId()), notNullValue());
    assertEquals(
      computadora.getId(),
      repositorio.buscarComputadoraConBloqueo(computadora.getId()).getId()
    );
  }

  @Test
  @Transactional
  @Rollback
  public void listadosDistinguenDisponibilidadYConservanOrdenDescendente() {
    ComputadoraUsada disponible = computadora("Dell", "A", true, 2);
    ComputadoraUsada sinStock = computadora("HP", "B", true, 0);
    ComputadoraUsada pausada = computadora("Lenovo", "C", false, 3);
    repositorio.guardarComputadora(disponible);
    repositorio.guardarComputadora(sinStock);
    repositorio.guardarComputadora(pausada);

    List<ComputadoraUsada> disponibles = repositorio.listarComputadorasDisponibles();
    List<ComputadoraUsada> todas = repositorio.listarComputadoras();

    assertThat(disponibles, contains(disponible));
    assertThat(todas, hasSize(3));
    assertThat(todas.get(0).getId() > todas.get(1).getId(), is(true));
  }

  @Test
  @Transactional
  @Rollback
  public void solicitudesSePersistenYSeListanPorFechaYCliente() {
    Usuario cliente = new Usuario();
    cliente.setEmail("cliente@bitfix.com");
    cliente.setPassword("clave");
    cliente.setRol("CLIENTE");
    cliente.setActivo(true);
    sessionFactory.getCurrentSession().persist(cliente);

    ComputadoraUsada computadora = computadora("Asus", "VivoBook", true, 1);
    repositorio.guardarComputadora(computadora);

    SolicitudCanje solicitud = new SolicitudCanje();
    solicitud.setCliente(cliente);
    solicitud.setComputadora(computadora);
    solicitud.setEquipoMarca("Acer");
    solicitud.setEquipoModelo("Aspire");
    solicitud.setEquipoDescripcion("Pantalla rota");
    solicitud.setValorOfrecido(20000.0);
    solicitud.setEstado(EstadoSolicitudCanje.PENDIENTE);
    repositorio.guardarSolicitud(solicitud);

    assertThat(repositorio.buscarSolicitud(solicitud.getId()), notNullValue());
    assertThat(repositorio.listarSolicitudes(), contains(solicitud));
    assertThat(repositorio.listarSolicitudesDeCliente(cliente.getId()), contains(solicitud));
  }

  private ComputadoraUsada computadora(String marca, String modelo, boolean activa, int stock) {
    ComputadoraUsada computadora = new ComputadoraUsada();
    computadora.setMarca(marca);
    computadora.setModelo(modelo);
    computadora.setCondicion("Reacondicionada");
    computadora.setPrecio(180000.0);
    computadora.setStock(stock);
    computadora.setActiva(activa);
    return computadora;
  }
}
