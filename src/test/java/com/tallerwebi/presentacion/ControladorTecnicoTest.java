package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;

public class ControladorTecnicoTest {

  @Mock
  private ServicioOrdenReparacion servicioOrdenReparacionMock;

  @Mock
  private ServicioPresupuesto servicioPresupuestoMock;

  @Mock
  private RepositorioUsuario repositorioUsuarioMock;

  @Mock
  private HttpSession sessionMock;

  private ControladorTecnico controladorTecnico;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);

    // Actualizamos el constructor inyectando el RepositorioUsuario
    controladorTecnico =
      new ControladorTecnico(
        servicioOrdenReparacionMock,
        servicioPresupuestoMock,
        repositorioUsuarioMock
      );
  }

  @Test
  public void mostrarPanelTecnico_deberiaMostrarOrdenesAsignadas() {
    Usuario tecnico = mock(Usuario.class);

    when(tecnico.getId()).thenReturn(10L);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(tecnico);

    List<OrdenReparacion> ordenes = Collections.singletonList(new OrdenReparacion());

    when(servicioOrdenReparacionMock.obtenerOrdenesDelTecnico(10L)).thenReturn(ordenes);

    ModelAndView resultado = controladorTecnico.mostrarPanelTecnico(sessionMock);

    assertThat(resultado.getViewName(), equalTo("panel-tecnico"));

    assertThat(
      (List<OrdenReparacion>) resultado.getModel().get("ordenesAsignadas"),
      contains(ordenes.get(0))
    );

    verify(servicioOrdenReparacionMock).obtenerOrdenesDelTecnico(10L);

    verify(servicioOrdenReparacionMock, never()).obtenerOrdenesParaTecnico();
  }

  //Para validar la vista del Historial Tecnico
  @Test
  public void queSePuedaIrALaVistaDeHistorialDesdeElPanelTecnico() {
    //Preparacion para evaluar el historial del tecnico en sesion
    Usuario tecnicoMock = new Usuario();
    tecnicoMock.setId(1L);
    tecnicoMock.setRol("TECNICO");
    when(sessionMock.getAttribute("USUARIO")).thenReturn(tecnicoMock);

    ModelAndView mav = controladorTecnico.verHistorialOrdenesCompletadas(sessionMock);

    assertEquals("panel-tecnico", mav.getViewName());
  }

  @Test
  public void queAlPedirElHistorialElModeloTengaLaListaDeOrdenesCompletadasDelTecnico() {
    Usuario tecnicoMock = new Usuario();
    tecnicoMock.setId(1L);
    tecnicoMock.setRol("TECNICO");
    when(sessionMock.getAttribute("USUARIO")).thenReturn(tecnicoMock);

    List<OrdenReparacion> listaOrdenes = new ArrayList<>();
    listaOrdenes.add(new OrdenReparacion());

    when(servicioOrdenReparacionMock.obtenerHistorialDelTecnico(1L)).thenReturn(listaOrdenes);

    ModelAndView mav = controladorTecnico.verHistorialOrdenesCompletadas(sessionMock);

    // Valida que la lista guardada sea exactamente la que mandó el servicio
    assertEquals(listaOrdenes, mav.getModel().get("historial"));
  }

  @Test
  public void queDerivarCotizacionExitosaRedirijaATriaje() throws NoHayTecnicosDisponibles {
    Usuario usuarioTecnico = new Usuario();
    usuarioTecnico.setRol("TECNICO");
    usuarioTecnico.setNivelSoporte(NivelReparacion.NIVEL_3);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioTecnico);

    doNothing().when(servicioOrdenReparacionMock).derivarCotizacion(1L, NivelReparacion.NIVEL_1);

    ModelAndView mav = controladorTecnico.derivarCotizacion(
      1L,
      NivelReparacion.NIVEL_1,
      sessionMock
    );

    assertEquals("redirect:/tecnico/triaje", mav.getViewName());
    verify(servicioOrdenReparacionMock, times(1)).derivarCotizacion(1L, NivelReparacion.NIVEL_1);
  }

  @Test
  public void queSiNoHayTecnicosDisponiblesAlDerivarVuelvaALaVistaTriajeConMensajeDeError()
    throws NoHayTecnicosDisponibles {
    Usuario usuarioTecnico = new Usuario();
    usuarioTecnico.setRol("TECNICO");
    usuarioTecnico.setNivelSoporte(NivelReparacion.NIVEL_3);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioTecnico);

    doThrow(
      new NoHayTecnicosDisponibles(
        "No hay técnicos disponibles con el nivel requerido para esta orden."
      )
    )
      .when(servicioOrdenReparacionMock)
      .derivarCotizacion(1L, NivelReparacion.NIVEL_1);

    when(servicioOrdenReparacionMock.listarTodas()).thenReturn(new ArrayList<>());

    ModelAndView mav = controladorTecnico.derivarCotizacion(
      1L,
      NivelReparacion.NIVEL_1,
      sessionMock
    );

    assertEquals("triaje-cotizaciones", mav.getViewName());
    assertTrue(mav.getModel().containsKey("error"));
    assertEquals(
      "No hay técnicos disponibles para el nivel seleccionado en este momento.",
      mav.getModel().get("error")
    );
    assertTrue(mav.getModel().containsKey("ordenes"));
  }

  // ==============================================================================
  // NUEVOS TESTS: SEGURIDAD Y FLUJO FÍSICO
  // ==============================================================================

  @Test
  public void queSiUnTecnicoNivel1IntentaAccederANuevaOrdenSeaRedirigidoAlPanel() {
    Usuario tecnicoNivel1 = new Usuario();
    tecnicoNivel1.setRol("TECNICO");
    tecnicoNivel1.setNivelSoporte(NivelReparacion.NIVEL_1); // No es Nivel 3

    when(sessionMock.getAttribute("USUARIO")).thenReturn(tecnicoNivel1);

    ModelAndView mav = controladorTecnico.nuevaOrden(sessionMock);

    assertEquals("redirect:/tecnico/panel-tecnico", mav.getViewName());
  }

  @Test
  public void queUnTecnicoNivel3PuedaRegistrarOrdenYSiEsClienteExistenteMuestreMensajeEstandar()
    throws Exception {
    Usuario tecnicoNivel3 = new Usuario();
    tecnicoNivel3.setRol("TECNICO");
    tecnicoNivel3.setNivelSoporte(NivelReparacion.NIVEL_3);

    DatosOrden datos = new DatosOrden();
    // No le seteamos passwordProvisoria simulando un cliente viejo

    when(sessionMock.getAttribute("USUARIO")).thenReturn(tecnicoNivel3);
    when(servicioOrdenReparacionMock.registrarOrdenEnLocal(datos))
      .thenReturn(new OrdenReparacion());
    when(servicioOrdenReparacionMock.obtenerOrdenesDelTecnico(any())).thenReturn(new ArrayList<>());

    ModelAndView mav = controladorTecnico.registrarOrden(datos, sessionMock);

    assertEquals("La orden ha sido registrada correctamente.", mav.getModel().get("exito"));
  }

  @Test
  public void queUnTecnicoNivel3PuedaRegistrarOrdenYSiEsClienteNuevoMuestreLaContrasena()
          throws Exception {
    Usuario tecnicoNivel3 = new Usuario();
    tecnicoNivel3.setRol("TECNICO");
    tecnicoNivel3.setNivelSoporte(NivelReparacion.NIVEL_3);

    DatosOrden datos = new DatosOrden();
    // Le seteamos email y teléfono para probar que viajen bien al modal
    datos.setEmailCliente("nuevo@test.com");
    datos.setTelefonoCliente("1123456789");

    // Configuramos el mock para que cuando el servicio trabaje con el DTO, le asigne una clave provisoria
    doAnswer(invocation -> {
      DatosOrden d = invocation.getArgument(0);
      d.setPasswordProvisoria("abc12345");
      return new OrdenReparacion();
    })
            .when(servicioOrdenReparacionMock)
            .registrarOrdenEnLocal(datos);

    when(sessionMock.getAttribute("USUARIO")).thenReturn(tecnicoNivel3);
    when(servicioOrdenReparacionMock.obtenerOrdenesDelTecnico(any())).thenReturn(new ArrayList<>());

    ModelAndView mav = controladorTecnico.registrarOrden(datos, sessionMock);

    // 1. Validamos el nuevo mensaje general
    assertEquals(
            "Equipo ingresado. El nuevo cliente fue registrado exitosamente.",
            mav.getModel().get("exito")
    );

    // 2. Validamos que se activen las banderas del Modal para WhatsApp
    assertEquals(true, mav.getModel().get("mostrarModalCredenciales"));
    assertEquals("abc12345", mav.getModel().get("modalPassword"));
    assertEquals("nuevo@test.com", mav.getModel().get("modalEmail"));
    assertEquals("1123456789", mav.getModel().get("modalTelefono"));
  }
}
