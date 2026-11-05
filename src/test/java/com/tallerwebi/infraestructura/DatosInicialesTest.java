package com.tallerwebi.infraestructura;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

public class DatosInicialesTest {

  @Test
  public void reiniciarNoReactivaUsuariosNiReemplazaSusDatos() {
    DriverManagerDataSource datasource = new DriverManagerDataSource("jdbc:hsqldb:mem:datos_iniciales", "sa", "");
    JdbcTemplate jdbc = new JdbcTemplate(datasource);
    jdbc.execute("SET DATABASE SQL SYNTAX MYS TRUE");
    jdbc.execute("create table Usuario (email varchar(255) unique, password varchar(255), " +
      "rol varchar(30), activo boolean, nivel_soporte varchar(30))");
    ResourceDatabasePopulator script = new ResourceDatabasePopulator(
      new FileSystemResource("src/main/resources/data.sql"));
    try {
      script.execute(datasource);
      assertEquals(5, jdbc.queryForObject("select count(*) from Usuario", Integer.class));
      verificarUsuarioDemo(jdbc, "tecnico.nivel1@bitfix.com", "TECNICO", "NIVEL_1");
      verificarUsuarioDemo(jdbc, "tecnico.nivel2@bitfix.com", "TECNICO", "NIVEL_2");
      verificarUsuarioDemo(jdbc, "tecnico.nivel3@bitfix.com", "TECNICO", "NIVEL_3");
      verificarUsuarioDemo(jdbc, "cliente@bitfix.com", "CLIENTE", null);
      String hash = jdbc.queryForObject("select password from Usuario where email='tecnico.nivel3@bitfix.com'", String.class);
      assertTrue(Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8().matches("1234", hash.substring(8)));
      jdbc.update("update Usuario set activo=false, nivel_soporte='NIVEL_1', password='clave-cambiada' " +
        "where email='tecnico.nivel3@bitfix.com'");
      script.execute(datasource);
      assertEquals(5, jdbc.queryForObject("select count(*) from Usuario", Integer.class));
      assertFalse(jdbc.queryForObject("select activo from Usuario where email='tecnico.nivel3@bitfix.com'", Boolean.class));
      assertEquals("NIVEL_1", jdbc.queryForObject("select nivel_soporte from Usuario where email='tecnico.nivel3@bitfix.com'", String.class));
      assertEquals("clave-cambiada", jdbc.queryForObject("select password from Usuario where email='tecnico.nivel3@bitfix.com'", String.class));
    } finally {
      jdbc.execute("drop table Usuario");
    }
  }

  private void verificarUsuarioDemo(JdbcTemplate jdbc, String email, String rol, String nivel) {
    assertEquals(rol, jdbc.queryForObject("select rol from Usuario where email=?", String.class, email));
    assertEquals(nivel, jdbc.queryForObject("select nivel_soporte from Usuario where email=?", String.class, email));
    assertTrue(jdbc.queryForObject("select activo from Usuario where email=?", Boolean.class, email));
    String hash = jdbc.queryForObject("select password from Usuario where email=?", String.class, email);
    assertTrue(Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8().matches("1234", hash.substring(8)));
  }
}
