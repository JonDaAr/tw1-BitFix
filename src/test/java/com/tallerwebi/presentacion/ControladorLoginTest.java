package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLoginTest {

  private ControladorLogin controladorLogin;
  private Usuario usuarioMock;
  private DatosLogin datosLoginMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private ServicioLogin servicioLoginMock;
  private ServicioOrdenReparacion servicioOrdenReparacionMock;

  @BeforeEach
  public void init() {
    datosLoginMock = new DatosLogin("dami@unlam.com", "123");

    usuarioMock = mock(Usuario.class);
    when(usuarioMock.getEmail()).thenReturn("dami@unlam.com");

    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);

    servicioLoginMock = mock(ServicioLogin.class);
    servicioOrdenReparacionMock = mock(ServicioOrdenReparacion.class);

    controladorLogin = new ControladorLogin(servicioLoginMock, servicioOrdenReparacionMock);
  }

  @Test
  public void loginConUsuarioYPasswordInorrectosDeberiaLlevarALoginNuevamente() {
    // preparacion
    when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(null);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));

    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Usuario o clave incorrecta")
    );

    verify(sessionMock, times(0)).setAttribute("ROL", "ADMIN");
  }

  @Test
  public void loginConUsuarioYPasswordCorrectosDeberiaLLevarAHome() {
    // preparacion
    Usuario usuarioEncontradoMock = mock(Usuario.class);

    when(usuarioEncontradoMock.getRol()).thenReturn("ADMIN");

    when(requestMock.getSession()).thenReturn(sessionMock);

    when(servicioLoginMock.consultarUsuario(anyString(), anyString()))
      .thenReturn(usuarioEncontradoMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));

    verify(sessionMock, times(1)).setAttribute("ROL", usuarioEncontradoMock.getRol());
  }

  @Test
  public void nuevoUsuarioDeberiaRetornarVistaNuevoUsuarioConDatosRegistroVacio() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.nuevoUsuario();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    // Ahora validamos que inyecte el DTO DatosRegistro en lugar de Usuario
    assertThat(modelAndView.getModel().get("datosRegistro"), instanceOf(DatosRegistro.class));
  }

  @Test
  public void registrarUsuarioConContrasenasQueCoincidenDeberiaRedirigirALogin()
    throws UsuarioExistente {
    // preparacion
    DatosRegistro datosRegistro = new DatosRegistro();
    datosRegistro.setEmail("nuevo@mail.com");
    datosRegistro.setPassword("123456");
    datosRegistro.setRepetirPassword("123456"); // Coinciden
    datosRegistro.setNombre("Juan");
    datosRegistro.setApellido("Perez");
    datosRegistro.setNombreUsuario("juanp");
    datosRegistro.setTelefono("1122334455");

    // ejecucion (asegurate de que el método en tu controlador se llame registrarUsuario o registrarme según lo hayas nombrado)
    ModelAndView modelAndView = controladorLogin.registrarUsuario(datosRegistro);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    verify(servicioLoginMock, times(1)).registrar(any(Usuario.class));
  }

  @Test
  public void registrarUsuarioConContrasenasDiferentesDeberiaMostrarError()
    throws UsuarioExistente {
    // preparacion
    DatosRegistro datosRegistro = new DatosRegistro();
    datosRegistro.setPassword("123456");
    datosRegistro.setRepetirPassword("654321"); // No coinciden

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarUsuario(datosRegistro);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Las contraseñas no coinciden")
    );
    verify(servicioLoginMock, never()).registrar(any(Usuario.class));
  }

  @Test
  public void registrarmeSiUsuarioExisteDeberiaVolverAFormularioYMostrarError()
    throws UsuarioExistente {
    // preparacion
    DatosRegistro datosRegistro = new DatosRegistro();
    datosRegistro.setEmail("existente@mail.com");
    datosRegistro.setPassword("123");
    datosRegistro.setRepetirPassword("123");

    doThrow(UsuarioExistente.class).when(servicioLoginMock).registrar(any(Usuario.class));

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarUsuario(datosRegistro);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("El email ya está registrado")
    );
  }

  @Test
  public void errorEnRegistrarmeDeberiaVolverAFormularioYMostrarError() throws UsuarioExistente {
    // preparacion
    DatosRegistro datosRegistro = new DatosRegistro();
    datosRegistro.setPassword("123");
    datosRegistro.setRepetirPassword("123");

    doThrow(RuntimeException.class).when(servicioLoginMock).registrar(any(Usuario.class));

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarUsuario(datosRegistro);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Error al registrar el usuario")
    );
  }

  @Test
  public void irALoginDeberiaRetornarVistaLoginConDatosLogin() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.irALogin();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));

    assertThat(modelAndView.getModel().get("datosLogin"), instanceOf(DatosLogin.class));
  }

  @Test
  public void nuevoUsuarioDeberiaRetornarVistaNuevoUsuarioConUsuarioVacio() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.nuevoUsuario();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(modelAndView.getModel().get("datosRegistro"), instanceOf(DatosRegistro.class));
  }

  @Test
  public void irAHomeDeberiaRetornarVistaHomeConOrdenesDelCliente() {
    // Preparación
    Usuario usuarioMock = mock(Usuario.class);
    when(usuarioMock.getEmail()).thenReturn("cliente@mail.com");
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    List<OrdenReparacion> ordenesMock = new ArrayList<>();
    when(servicioOrdenReparacionMock.obtenerOrdenesDelCliente("cliente@mail.com"))
      .thenReturn(ordenesMock);

    // Ejecución
    ModelAndView modelAndView = controladorLogin.irAHome(sessionMock);

    // Validación
    assertEquals("home", modelAndView.getViewName());
    assertEquals(ordenesMock, modelAndView.getModel().get("ordenes"));
    verify(servicioOrdenReparacionMock, times(1)).obtenerOrdenesDelCliente("cliente@mail.com");
    verify(servicioOrdenReparacionMock, times(1)).obtenerOrdenesDelCliente("cliente@mail.com");
  }

  @Test
  public void inicioDeberiaRedirigirALogin() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.inicio();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void loginConUsuarioTecnicoCorrectoDeberiaLlevarAlPanelTecnico() {
    // preparacion
    Usuario usuarioEncontradoMock = mock(Usuario.class);

    when(usuarioEncontradoMock.getRol()).thenReturn("TECNICO");

    when(requestMock.getSession()).thenReturn(sessionMock);

    when(servicioLoginMock.consultarUsuario(anyString(), anyString()))
      .thenReturn(usuarioEncontradoMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/tecnico/panel-tecnico"));

    verify(sessionMock, times(1)).setAttribute("ROL", usuarioEncontradoMock.getRol());
  }
}
