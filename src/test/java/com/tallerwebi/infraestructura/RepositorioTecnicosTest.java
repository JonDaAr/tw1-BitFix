package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioTecnicosTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioUsuario repositorioUsuario;

  @BeforeEach
  public void init() {
    repositorioUsuario = new RepositorioUsuarioImpl(sessionFactory);
  }

  @Test
  @Transactional
  public void deberiaBuscarSoloTecnicosActivos() {
    persistirUsuario("tecnico.activo@test.com", "TECNICO", true);
    persistirUsuario("tecnico.inactivo@test.com", "TECNICO", false);
    persistirUsuario("admin@test.com", "ADMIN", true);
    sessionFactory.getCurrentSession().flush();

    List<Usuario> tecnicos = repositorioUsuario.buscarTecnicosActivos();

    assertThat(tecnicos, hasSize(1));
    assertThat(tecnicos.get(0).getRol(), is("TECNICO"));
    assertThat(tecnicos.get(0).getActivo(), is(true));
  }

  private void persistirUsuario(String email, String rol, boolean activo) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("1234");
    usuario.setRol(rol);
    usuario.setActivo(activo);
    sessionFactory.getCurrentSession().persist(usuario);
  }
}
