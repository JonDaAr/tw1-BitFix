package com.tallerwebi.infraestructura;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.ServicioLoginImpl;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
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
public class LoginPersistenciaTest {

  @Autowired
  private SessionFactory sessionFactory;
  private RepositorioUsuarioImpl repositorio;
  private ServicioLoginImpl servicio;

  @BeforeEach
  public void preparar() {
    repositorio = new RepositorioUsuarioImpl(sessionFactory);
    servicio = new ServicioLoginImpl(repositorio, new RepositorioOrdenReparacionImpl(sessionFactory));
  }

  @Test
  public void registroPersisteHashYLaBajaImpideLogin() throws Exception {
    Usuario usuario = usuario("nuevo-hash@test.com");
    servicio.registrar(usuario);
    recargar();
    Usuario guardado = repositorio.buscar(usuario.getEmail());
    assertTrue(guardado.getPassword().startsWith("{pbkdf2}"));
    assertNotEquals("clave-prueba", guardado.getPassword());
    assertNotNull(servicio.consultarUsuario(usuario.getEmail(), "clave-prueba"));
    guardado.setActivo(false);
    repositorio.modificar(guardado);
    recargar();
    assertNull(servicio.consultarUsuario(usuario.getEmail(), "clave-prueba"));
  }

  @Test
  public void loginDeCuentaAntiguaPersisteLaMigracion() {
    Usuario antiguo = usuario("antiguo-hash@test.com");
    repositorio.guardar(antiguo);
    recargar();
    assertNotNull(servicio.consultarUsuario(antiguo.getEmail(), "clave-prueba"));
    recargar();
    String hashGuardado = repositorio.buscar(antiguo.getEmail()).getPassword();
    assertTrue(hashGuardado.startsWith("{pbkdf2}"));
    assertNotNull(servicio.consultarUsuario(antiguo.getEmail(), "clave-prueba"));
    recargar();
    assertEquals(hashGuardado, repositorio.buscar(antiguo.getEmail()).getPassword());
  }

  private Usuario usuario(String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("clave-prueba");
    usuario.setActivo(true);
    usuario.setRol("CLIENTE");
    return usuario;
  }

  private void recargar() {
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();
  }
}
