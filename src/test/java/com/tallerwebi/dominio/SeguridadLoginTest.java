package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SeguridadLoginTest {

  private RepositorioUsuario repositorio;
  private ServicioLoginImpl servicio;

  @BeforeEach
  public void preparar() {
    repositorio = mock(RepositorioUsuario.class);
    servicio = new ServicioLoginImpl(repositorio, mock(RepositorioOrdenReparacion.class));
  }

  private Usuario usuario(String password) {
    Usuario usuario = new Usuario();
    usuario.setEmail("cliente@test.com");
    usuario.setPassword(password);
    usuario.setActivo(true);
    return usuario;
  }

  @Test
  public void registroGuardaHashConSalYPermiteLoginSinReescribirlo() throws Exception {
    Usuario primero = usuario("clave-segura");
    Usuario segundo = usuario("clave-segura");
    servicio.registrar(primero);
    servicio.registrar(segundo);
    assertNotEquals("clave-segura", primero.getPassword());
    assertNotEquals(primero.getPassword(), segundo.getPassword());
    when(repositorio.buscar(primero.getEmail())).thenReturn(primero);
    assertSame(primero, servicio.consultarUsuario(primero.getEmail(), "clave-segura"));
    assertNull(servicio.consultarUsuario(primero.getEmail(), "incorrecta"));
    assertNull(servicio.consultarUsuario(primero.getEmail(), primero.getPassword()));
    verify(repositorio, never()).modificar(any());
  }

  @Test
  public void loginCorrectoMigraLaClaveAntiguaUnaSolaVez() {
    Usuario antiguo = usuario("clave-antigua");
    when(repositorio.buscar(antiguo.getEmail())).thenReturn(antiguo);
    assertSame(antiguo, servicio.consultarUsuario(antiguo.getEmail(), "clave-antigua"));
    assertTrue(antiguo.getPassword().startsWith("{pbkdf2}"));
    assertSame(antiguo, servicio.consultarUsuario(antiguo.getEmail(), "clave-antigua"));
    verify(repositorio, times(1)).modificar(antiguo);
  }

  @Test
  public void loginIncorrectoNoMigraNiModificaCredenciales() {
    Usuario antiguo = usuario("clave-antigua");
    when(repositorio.buscar(antiguo.getEmail())).thenReturn(antiguo);
    assertNull(servicio.consultarUsuario(antiguo.getEmail(), "incorrecta"));
    assertEquals("clave-antigua", antiguo.getPassword());
    verify(repositorio, never()).modificar(any());
  }

  @Test
  public void cuentaInactivaNoIniciaSesionNiMigraSuClave() {
    for (Boolean activo : new Boolean[] { false, null }) {
      Usuario inactivo = usuario("clave-antigua");
      inactivo.setActivo(activo);
      when(repositorio.buscar(inactivo.getEmail())).thenReturn(inactivo);
      assertNull(servicio.consultarUsuario(inactivo.getEmail(), "clave-antigua"));
      assertEquals("clave-antigua", inactivo.getPassword());
    }
    verify(repositorio, never()).modificar(any());
  }

  @Test
  public void emailExistenteConOtraClaveProduceUsuarioExistente() {
    when(repositorio.buscar("cliente@test.com")).thenReturn(usuario("clave-original"));
    assertThrows(UsuarioExistente.class, () -> servicio.registrar(usuario("otra-clave")));
    verify(repositorio, never()).guardar(any());
  }

  @Test
  public void credencialesIncompletasYHashCorruptoNoAutentican() {
    assertNull(servicio.consultarUsuario(null, "clave"));
    assertNull(servicio.consultarUsuario("cliente@test.com", null));
    assertNull(servicio.consultarUsuario("cliente@test.com", ""));
    assertNull(servicio.consultarUsuario("inexistente@test.com", "clave"));
    for (String almacenada : new String[] { null, "{pbkdf2}hash-invalido" }) {
      when(repositorio.buscar("cliente@test.com")).thenReturn(usuario(almacenada));
      assertNull(servicio.consultarUsuario("cliente@test.com", "clave"));
    }
  }

  @Test
  public void noPermiteRegistrarUnaClaveVacia() {
    assertThrows(IllegalArgumentException.class, () -> servicio.registrar(usuario("")));
    verify(repositorio, never()).guardar(any());
  }
}
