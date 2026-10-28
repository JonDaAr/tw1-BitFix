package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.RepositorioRepuesto;
import com.tallerwebi.dominio.Repuesto;
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
public class RepositorioRepuestoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioRepuesto repositorioRepuesto;

  @BeforeEach
  public void init() {
    this.repositorioRepuesto = new RepositorioRepuestoImpl(this.sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueExisteUnRepuesto_cuandoSeGuarda_entoncesSePuedeRecuperarPorId() {
    Repuesto repuesto = new Repuesto("Modulo Pantalla Samsung", 45000.0, 5);

    this.repositorioRepuesto.guardar(repuesto);
    Repuesto buscado = this.repositorioRepuesto.buscarPorId(repuesto.getId());

    assertThat(buscado, notNullValue());
    assertThat(buscado.getNombre(), equalTo("Modulo Pantalla Samsung"));
  }

  @Test
  @Transactional
  @Rollback
  public void cuandoSePidenDisponibles_soloDebeRetornarAquellosConStockMayorACero() {
    Repuesto conStock = new Repuesto("SSD 480GB", 32000.0, 3);
    Repuesto sinStock = new Repuesto("Bateria Dell", 25000.0, 0);

    this.repositorioRepuesto.guardar(conStock);
    this.repositorioRepuesto.guardar(sinStock);

    List<Repuesto> disponibles = this.repositorioRepuesto.obtenerDisponibles();

    assertThat(disponibles, hasSize(1));
    assertThat(disponibles.get(0).getNombre(), equalTo("SSD 480GB"));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueExisteUnRepuesto_cuandoSeElimina_entoncesYaNoSeEncuentraEnLaBase() {
    // Dado
    Repuesto repuesto = new Repuesto();
    repuesto.setNombre("Pieza a borrar");
    repuesto.setPrecio(100.0);
    repuesto.setStock(1);
    repositorioRepuesto.guardar(repuesto);

    // Cuando
    repositorioRepuesto.eliminar(repuesto);

    // Entonces
    Repuesto recuperado = repositorioRepuesto.buscarPorId(repuesto.getId());
    org.hamcrest.MatcherAssert.assertThat(recuperado, org.hamcrest.Matchers.nullValue());
  }

  @Test
  @Transactional
  @Rollback
  public void obtenerTodosDeberiaRetornarDosRepuestos() {
    Repuesto repuesto1 = new Repuesto("SSD 480GB", 32000.0, 3);
    Repuesto repuesto2 = new Repuesto("Bateria Dell", 25000.0, 5);

    this.repositorioRepuesto.guardar(repuesto1);
    this.repositorioRepuesto.guardar(repuesto2);

    List<Repuesto> resultado = this.repositorioRepuesto.obtenerTodos();

    assertThat(resultado, notNullValue());
    assertThat(resultado, hasSize(2));
    assertThat(
      resultado,
      containsInAnyOrder(
        hasProperty("nombre", equalTo("SSD 480GB")),
        hasProperty("nombre", equalTo("Bateria Dell"))
      )
    );
  }

  @Test
  @Transactional
  @Rollback
  public void buscarPorIdCuandoIdEsNuloDeberiaRetornarNull() {
    Repuesto resultado = this.repositorioRepuesto.buscarPorId(null);
    assertThat(resultado, nullValue());
  }
}
