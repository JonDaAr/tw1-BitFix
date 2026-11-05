package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.presentacion.DatosRegistro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioLoginTest {

  private RepositorioUsuario repositorioUsuarioMock;
  private RepositorioOrdenReparacion repositorioOrdenReparacionMock;
  private ServicioLoginImpl servicioLogin;

  @BeforeEach
  public void init() {
    // 1. Creamos los mocks de ambos repositorios
    repositorioUsuarioMock = mock(RepositorioUsuario.class);
    repositorioOrdenReparacionMock = mock(RepositorioOrdenReparacion.class);

    // 2. Inyectamos ambos al servicio tal como configuramos en el constructor
    servicioLogin = new ServicioLoginImpl(repositorioUsuarioMock, repositorioOrdenReparacionMock);
  }

  @Test
  public void consultarUsuarioDevuelveUsuarioSiExiste() {
    Usuario usuarioEsperado = new Usuario();
    usuarioEsperado.setEmail("test@test.com");
    usuarioEsperado.setPassword("1234");
    usuarioEsperado.setActivo(true);

    when(repositorioUsuarioMock.buscar("test@test.com")).thenReturn(usuarioEsperado);

    Usuario usuarioObtenido = servicioLogin.consultarUsuario("test@test.com", "1234");

    assertThat(usuarioObtenido, is(equalTo(usuarioEsperado)));
  }

  @Test
  public void registrarLanzaExcepcionSiElUsuarioYaExiste() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setEmail("test@test.com");
    usuario.setPassword("1234");

    // Mockeamos ambas firmas para cubrir cómo viene el proyecto base
    when(repositorioUsuarioMock.buscar("test@test.com")).thenReturn(new Usuario());

    assertThrows(
      UsuarioExistente.class,
      () -> {
        servicioLogin.registrar(usuario);
      }
    );
  }

  // --- TESTS NUEVOS DE LA GESTIÓN DE TÉCNICOS ---

  @Test
  public void deberiaLanzarExcepcionAlIntentarEliminarTecnicoConOrdenesActivas() {
    // Preparación
    Long idTecnico = 1L;

    // Simulamos que el repositorio encuentra 2 órdenes activas para este técnico
    when(repositorioOrdenReparacionMock.contarOrdenesActivas(idTecnico)).thenReturn(2L);

    // Validación: Esperamos que lance IllegalStateException
    assertThrows(
      IllegalStateException.class,
      () -> {
        servicioLogin.eliminarUsuario(idTecnico);
      }
    );

    // Verificamos que el repositorio de usuario NUNCA llamó a modificar (no se dio de baja)
    verify(repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void deberiaDarDeBajaAlTecnicoSiNoTieneOrdenesActivas() {
    // Preparación
    Long idTecnico = 1L;
    Usuario tecnico = new Usuario();
    tecnico.setId(idTecnico);
    tecnico.setActivo(true);

    // Simulamos que el repositorio NO encuentra órdenes activas
    when(repositorioOrdenReparacionMock.contarOrdenesActivas(idTecnico)).thenReturn(0L);
    // Simulamos que encuentra al usuario en la BD
    when(repositorioUsuarioMock.buscarPorId(idTecnico)).thenReturn(tecnico);

    // Ejecución
    servicioLogin.eliminarUsuario(idTecnico);

    // Validación
    assertThat(tecnico.getActivo(), is(false)); // Se hizo la baja lógica
    verify(repositorioUsuarioMock, times(1)).modificar(tecnico); // Se guardó el cambio en BD
  }

  @Test
  public void buscarUsuarioPorIdDeberiaLlamarAlRepositorio() {
    Usuario tecnicoMock = new Usuario();
    when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(tecnicoMock);

    Usuario resultado = servicioLogin.buscarUsuarioPorId(1L);

    assertThat(resultado, equalTo(tecnicoMock));
    verify(repositorioUsuarioMock, times(1)).buscarPorId(1L);
  }

  @Test
  public void actualizarDatosTecnicoDeberiaModificarCamposBasicosSinTocarPasswordSiVieneVacia() {
    // Preparación
    Usuario tecnicoExistente = new Usuario();
    tecnicoExistente.setPassword("pass-vieja");
    when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(tecnicoExistente);

    DatosRegistro datosNuevos = new DatosRegistro();
    datosNuevos.setNombre("Nuevo Nombre");
    datosNuevos.setApellido("Nuevo Apellido");
    datosNuevos.setEmail("nuevo@mail.com");
    // Simulamos que el admin dejó la contraseña vacía
    datosNuevos.setPassword("");

    // Ejecución
    servicioLogin.actualizarDatosTecnico(1L, datosNuevos);

    // Validación
    assertThat(tecnicoExistente.getNombre(), equalTo("Nuevo Nombre"));
    assertThat(tecnicoExistente.getEmail(), equalTo("nuevo@mail.com"));
    assertThat(tecnicoExistente.getPassword(), equalTo("pass-vieja")); // No debió cambiar
    verify(repositorioUsuarioMock, times(1)).modificar(tecnicoExistente);
  }

  @Test
  public void actualizarDatosTecnicoDeberiaModificarPasswordSiVieneConDatos() {
    // Preparación
    Usuario tecnicoExistente = new Usuario();
    tecnicoExistente.setPassword("pass-vieja");
    when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(tecnicoExistente);

    DatosRegistro datosNuevos = new DatosRegistro();
    datosNuevos.setPassword("pass-nueva123");

    // Ejecución
    servicioLogin.actualizarDatosTecnico(1L, datosNuevos);

    // Validación
    org.junit.jupiter.api.Assertions.assertTrue(tecnicoExistente.getPassword().startsWith("{pbkdf2}"));
    org.junit.jupiter.api.Assertions.assertTrue(
      org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()
        .matches("pass-nueva123", tecnicoExistente.getPassword().substring("{pbkdf2}".length()))
    );
    verify(repositorioUsuarioMock, times(1)).modificar(tecnicoExistente);
  }
}
