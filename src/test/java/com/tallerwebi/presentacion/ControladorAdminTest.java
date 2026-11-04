package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorAdminTest {

  private ControladorAdmin controladorAdmin;
  private ServicioLogin servicioLoginMock;
  private HttpSession sessionMock;
  private Usuario usuarioMock;

  private static final String ATRIBUTO_USUARIO = "USUARIO";
  private static final String ROL_ADMIN = "ADMIN";
  private static final String ROL_TECNICO = "TECNICO";

  @BeforeEach
  public void init() {
    servicioLoginMock = mock(ServicioLogin.class);
    controladorAdmin = new ControladorAdmin(servicioLoginMock);
    sessionMock = mock(HttpSession.class);
    usuarioMock = mock(Usuario.class);
  }

  // ==========================================
  // 1. TESTS DEL PANEL PRINCIPAL
  // ==========================================

  @Test
  public void siNoHayUsuarioEnSesionAlIntentarIrAlPanelDeberiaRedirigirAlLogin() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(null);

    ModelAndView mav = controladorAdmin.irAPanelAdmin(sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void siElUsuarioEnSesionNoEsAdminAlIntentarIrAlPanelDeberiaRedirigirAlLogin() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_TECNICO);

    ModelAndView mav = controladorAdmin.irAPanelAdmin(sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void siElUsuarioEnSesionEsAdminAlIntentarIrAlPanelDeberiaMostrarPanelAdmin() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);

    ModelAndView mav = controladorAdmin.irAPanelAdmin(sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("panel-admin"));
  }

  // ==========================================
  // 2. TESTS DEL FORMULARIO DE NUEVO TÉCNICO
  // ==========================================

  @Test
  public void siElUsuarioNoEsAdminAlIntentarIrAFormularioTecnicoDeberiaRedirigirAlLogin() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_TECNICO);

    ModelAndView mav = controladorAdmin.irANuevoTecnico(sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void siElUsuarioEsAdminAlIntentarIrAFormularioTecnicoDeberiaMostrarLaVistaConElModelo() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);

    ModelAndView mav = controladorAdmin.irANuevoTecnico(sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("admin-nuevo-tecnico"));
    assertThat(mav.getModel().get("datosRegistro"), notNullValue());
  }

  // ==========================================
  // 3. TESTS DEL ALTA DEL TÉCNICO (GUARDADO)
  // ==========================================

  @Test
  public void siElUsuarioNoEsAdminAlGuardarTecnicoDeberiaRedirigirAlLogin()
    throws UsuarioExistente {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_TECNICO);
    DatosRegistro datosRegistro = new DatosRegistro();

    ModelAndView mav = controladorAdmin.guardarTecnico(datosRegistro, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
    verify(servicioLoginMock, never()).registrar(any(Usuario.class));
  }

  @Test
  public void siElUsuarioEsAdminYLosDatosSonValidosDeberiaGuardarYRedirigirAlPanel()
    throws Exception {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);
    DatosRegistro datosRegistro = new DatosRegistro();

    ModelAndView mav = controladorAdmin.guardarTecnico(datosRegistro, sessionMock);

    assertThat(
      mav.getViewName(),
      equalToIgnoringCase("redirect:/admin/panel?exito=tecnico_creado")
    );
    verify(servicioLoginMock, times(1)).registrar(any(Usuario.class));
  }

  @Test
  public void siElEmailYaExisteAlGuardarTecnicoDeberiaMostrarMensajeDeErrorEnFormulario()
    throws Exception {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);
    DatosRegistro datosRegistro = new DatosRegistro();

    doThrow(UsuarioExistente.class).when(servicioLoginMock).registrar(any(Usuario.class));

    ModelAndView mav = controladorAdmin.guardarTecnico(datosRegistro, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("admin-nuevo-tecnico"));
    assertThat(
      mav.getModel().get("error").toString(),
      equalToIgnoringCase("El email ya está registrado en el sistema.")
    );
  }

  // ==========================================
  // 4. TESTS DE ELIMINACIÓN DE TÉCNICOS
  // ==========================================

  @Test
  public void deberiaRedirigirConErrorAlEliminarSiElTecnicoTieneOrdenesPendientes() {
    Long idTecnico = 1L;
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);

    doThrow(new IllegalStateException("El técnico tiene órdenes activas."))
      .when(servicioLoginMock)
      .eliminarUsuario(idTecnico);

    ModelAndView mav = controladorAdmin.eliminarTecnico(idTecnico, sessionMock);

    assertThat(
      mav.getViewName(),
      equalToIgnoringCase("redirect:/admin/tecnicos?error=con_pendientes")
    );
  }

  @Test
  public void deberiaRedirigirConExitoAlEliminarUnTecnicoSinOrdenesPendientes() {
    Long idTecnico = 1L;
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);

    doNothing().when(servicioLoginMock).eliminarUsuario(idTecnico);

    ModelAndView mav = controladorAdmin.eliminarTecnico(idTecnico, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/admin/tecnicos?exito=eliminado"));
  }
}
