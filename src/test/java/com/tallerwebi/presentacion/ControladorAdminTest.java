package com.tallerwebi.presentacion;

import static com.tallerwebi.presentacion.ControladorAdmin.ATRIBUTO_DATOS_REGISTRO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorAdminTest {

  private static final String ATRIBUTO_DATOS_REGISTRO = "datosRegistro";
  private ControladorAdmin controladorAdmin;
  private ServicioLogin servicioLoginMock;
  private ServicioOrdenReparacion servicioOrdenReparacionMock; // NUEVO
  private RepositorioUsuario repositorioUsuarioMock; // NUEVO
  private HttpSession sessionMock;
  private Usuario usuarioMock;

  private static final String ATRIBUTO_USUARIO = "USUARIO";
  private static final String ROL_ADMIN = "ADMIN";
  private static final String ROL_TECNICO = "TECNICO";

  @BeforeEach
  public void init() {
    servicioLoginMock = mock(ServicioLogin.class);
    servicioOrdenReparacionMock = mock(ServicioOrdenReparacion.class); // NUEVO
    repositorioUsuarioMock = mock(RepositorioUsuario.class); // NUEVO

    // ACTUALIZADO (asegurate de que coincida con el constructor de tu ControladorAdmin real)
    controladorAdmin =
      new ControladorAdmin(servicioLoginMock, servicioOrdenReparacionMock, repositorioUsuarioMock);

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

  // TESTS DE LISTADO DE TECNICOS
  @Test
  public void siNoHayUsuarioEnSesionAlIntentarListarTecnicosDeberiaRedirigirAlLogin() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(null);

    ModelAndView mav = controladorAdmin.listarTecnicos(sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void siElUsuarioEnSesionNoEsAdminAlIntentarListarTecnicosDeberiaRedirigirAlLogin() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_TECNICO);

    ModelAndView mav = controladorAdmin.listarTecnicos(sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void siElUsuarioEnSesionEsAdminAlIntentarListarTecnicosDeberiaMostrarVistaConLista() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);

    List<Usuario> tecnicosMock = new ArrayList<>();
    when(servicioLoginMock.obtenerUsuariosPorRol("TECNICO")).thenReturn(tecnicosMock);

    ModelAndView mav = controladorAdmin.listarTecnicos(sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("admin-tecnicos"));
    assertThat(mav.getModel().get("tecnicos"), notNullValue());
    verify(servicioLoginMock, times(1)).obtenerUsuariosPorRol("TECNICO");
  }

  // TEST PARA ERRORES AL GUARDAR TÉCNICO

  @Test
  public void siOcurreUnErrorInesperadoAlGuardarTecnicoDeberiaMostrarMensajeDeErrorEnFormulario()
    throws Exception {
    //Preparacion
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);
    DatosRegistro datosRegistro = new DatosRegistro();

    doThrow(new RuntimeException("Error de base de datos"))
      .when(servicioLoginMock)
      .registrar(any(Usuario.class));

    //Ejecucion
    ModelAndView mav = controladorAdmin.guardarTecnico(datosRegistro, sessionMock);

    //Validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("admin-nuevo-tecnico"));
    assertThat(
      mav.getModel().get("error").toString(),
      equalToIgnoringCase("Ocurrió un error al registrar al técnico.")
    );
    assertThat(mav.getModel().get("datosRegistro"), equalTo(datosRegistro));
  }

  // ==========================================
  // 5. TESTS DEL MÓDULO DE CONTINGENCIA
  // ==========================================

  @Test
  public void siNoEsAdminAlIntentarIrAContingenciaDeberiaRedirigirAlLogin() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_TECNICO);

    ModelAndView mav = controladorAdmin.irAModuloContingencia(sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void siEsAdminAlIntentarIrAContingenciaDeberiaMostrarLaVistaConOrdenesYTecnicos() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);

    List<OrdenReparacion> ordenesMock = new ArrayList<>();
    List<Usuario> tecnicosMock = new ArrayList<>();

    when(servicioOrdenReparacionMock.listarTodas()).thenReturn(ordenesMock);
    when(servicioLoginMock.obtenerTecnicosActivos()).thenReturn(tecnicosMock);

    ModelAndView mav = controladorAdmin.irAModuloContingencia(sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("admin-contingencia"));
    assertThat(mav.getModel().get("ordenes"), notNullValue());
    assertThat(mav.getModel().get("tecnicos"), notNullValue());
  }

  @Test
  public void siNoEsAdminAlReasignarOrdenDeberiaRedirigirAlLogin() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_TECNICO);

    ModelAndView mav = controladorAdmin.procesarReasignacion(1L, 2L, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
    verify(servicioOrdenReparacionMock, never()).reasignarOrden(anyLong(), anyLong());
  }

  @Test
  public void siEsAdminYLaReasignacionEsExitosaDeberiaRedirigirConExito() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);

    doNothing().when(servicioOrdenReparacionMock).reasignarOrden(1L, 2L);

    ModelAndView mav = controladorAdmin.procesarReasignacion(1L, 2L, sessionMock);

    assertThat(
      mav.getViewName(),
      equalToIgnoringCase("redirect:/admin/contingencia?exito=reasignado")
    );
    verify(servicioOrdenReparacionMock, times(1)).reasignarOrden(1L, 2L);
  }

  @Test
  public void siOcurreUnErrorAlReasignarDeberiaRedirigirConError() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);

    doThrow(new IllegalArgumentException("Error de prueba"))
      .when(servicioOrdenReparacionMock)
      .reasignarOrden(1L, 2L);

    ModelAndView mav = controladorAdmin.procesarReasignacion(1L, 2L, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/admin/contingencia?error=fallo"));
  }

  // ==========================================
  // 6. TESTS DE EDICIÓN DE TÉCNICO
  // ==========================================

  @Test
  public void siNoEsAdminAlIntentarIrAEditarTecnicoDeberiaRedirigirAlLogin() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_TECNICO);

    ModelAndView mav = controladorAdmin.irAEditarTecnico(1L, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void siTecnicoNoExisteAlIntentarEditarDeberiaRedirigirConError() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);
    when(servicioLoginMock.buscarUsuarioPorId(1L)).thenReturn(null);

    ModelAndView mav = controladorAdmin.irAEditarTecnico(1L, sessionMock);

    assertThat(
      mav.getViewName(),
      equalToIgnoringCase("redirect:/admin/tecnicos?error=no_encontrado")
    );
  }

  @Test
  public void siEsAdminYTecnicoExisteDeberiaMostrarVistaDeEdicionConDatosCargados() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);

    Usuario tecnicoMock = new Usuario();
    tecnicoMock.setNombre("Juan");
    when(servicioLoginMock.buscarUsuarioPorId(1L)).thenReturn(tecnicoMock);

    ModelAndView mav = controladorAdmin.irAEditarTecnico(1L, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("admin-editar-tecnico"));
    assertThat(mav.getModel().get(ATRIBUTO_DATOS_REGISTRO), notNullValue());
    assertThat(mav.getModel().get("idTecnico"), equalTo(1L));
  }

  @Test
  public void siNoEsAdminAlActualizarTecnicoDeberiaRedirigirAlLogin() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_TECNICO);
    DatosRegistro datos = new DatosRegistro();

    ModelAndView mav = controladorAdmin.actualizarTecnico(1L, datos, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
    verify(servicioLoginMock, never()).actualizarDatosTecnico(anyLong(), any(DatosRegistro.class));
  }

  @Test
  public void siEsAdminYActualizacionEsExitosaDeberiaRedirigirAExito() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);
    DatosRegistro datos = new DatosRegistro();

    doNothing().when(servicioLoginMock).actualizarDatosTecnico(1L, datos);

    ModelAndView mav = controladorAdmin.actualizarTecnico(1L, datos, sessionMock);

    assertThat(
      mav.getViewName(),
      equalToIgnoringCase("redirect:/admin/tecnicos?exito=actualizado")
    );
    verify(servicioLoginMock, times(1)).actualizarDatosTecnico(1L, datos);
  }

  @Test
  public void siFallaLaActualizacionDeberiaDevolverAVistaConError() {
    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);
    DatosRegistro datos = new DatosRegistro();

    doThrow(new RuntimeException("Error bd"))
      .when(servicioLoginMock)
      .actualizarDatosTecnico(1L, datos);

    ModelAndView mav = controladorAdmin.actualizarTecnico(1L, datos, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("admin-editar-tecnico"));
    assertThat(mav.getModel().get("error"), notNullValue());
    assertThat(mav.getModel().get(ATRIBUTO_DATOS_REGISTRO), equalTo(datos));
  }
}
