package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.RepositorioSucursal;
import com.tallerwebi.dominio.Sucursal;
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
public class RepositorioSucursalTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioSucursal repositorioSucursal;

  @BeforeEach
  public void init() {
    this.repositorioSucursal = new RepositorioSucursalImpl(this.sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueExistenSucursalesCuandoSeObtienenTodas_entoncesRetornaLaListaCompleta() {
    // Given
    Sucursal sucursal1 = new Sucursal();
    sucursal1.setNombre("Sucursal San Justo");
    sucursal1.setDireccion("Arieta 1234");
    sucursal1.setLatitud(-34.6781);
    sucursal1.setLongitud(-58.5636);

    Sucursal sucursal2 = new Sucursal();
    sucursal2.setNombre("Sucursal Ramos Mejía");
    sucursal2.setDireccion("Avenida de Mayo 500");
    sucursal2.setLatitud(-34.6471);
    sucursal2.setLongitud(-58.5631);

    this.sessionFactory.getCurrentSession().save(sucursal1);
    this.sessionFactory.getCurrentSession().save(sucursal2);

    // When
    List<Sucursal> resultado = this.repositorioSucursal.obtenerTodas();

    // Then
    assertThat(resultado, notNullValue());
    assertThat(resultado, hasSize(2));
    assertThat(
      resultado,
      containsInAnyOrder(
        hasProperty("nombre", equalTo("Sucursal San Justo")),
        hasProperty("nombre", equalTo("Sucursal Ramos Mejía"))
      )
    );
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueNoExistenSucursales_cuandoSeObtienenTodas_entoncesRetornaListaVacia() {
    // When
    List<Sucursal> resultado = this.repositorioSucursal.obtenerTodas();

    // Then
    assertThat(resultado, notNullValue());
    assertThat(resultado, is(empty()));
  }
}
