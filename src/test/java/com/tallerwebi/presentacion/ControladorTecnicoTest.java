package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.ServicioPresupuesto;
import com.tallerwebi.dominio.Usuario;
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
  private HttpSession sessionMock;

  private ControladorTecnico controladorTecnico;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);

    controladorTecnico =
      new ControladorTecnico(servicioOrdenReparacionMock, servicioPresupuestoMock);
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

  //  @Test
  //  public void enviarPresupuestoCliente_cuandoEsExitoso_deberiaMostrarMensaje() {
  //    Usuario tecnico = mock(Usuario.class);
  //
  //    when(tecnico.getId()).thenReturn(10L);
  //    when(sessionMock.getAttribute("USUARIO")).thenReturn(tecnico);
  //
  //    List<OrdenReparacion> ordenes = Collections.emptyList();
  //
  //    when(servicioOrdenReparacionMock.obtenerOrdenesDelTecnico(10L)).thenReturn(ordenes);
  //
  //    ModelAndView resultado = controladorTecnico.enviarPresupuestoCliente(
  //      1001,
  //      15000.0,
  //      5,
  //      "Cambio de placa madre",
  //      sessionMock
  //    );
  //
  //    assertThat(resultado.getViewName(), equalTo("panel-tecnico"));
  //
  //    assertThat(
  //      resultado.getModel().get("mensaje"),
  //      equalTo("El presupuesto para la orden #1001 se envió correctamente al cliente.")
  //    );
  //
  //    assertThat(resultado.getModel().get("ordenesAsignadas"), equalTo(ordenes));
  //
  //    verify(servicioPresupuestoMock)
  //      .generarYEnviarPresupuesto(1001, 15000.0, "Cambio de placa madre");
  //
  //    verify(servicioOrdenReparacionMock).obtenerOrdenesDelTecnico(10L);
  //
  //    verify(servicioOrdenReparacionMock, never()).obtenerOrdenesParaTecnico();
  //  }

  //  @Test
  //  public void enviarPresupuestoCliente_cuandoOcurreError_deberiaMostrarError() {
  //    Usuario tecnico = mock(Usuario.class);
  //
  //    when(tecnico.getId()).thenReturn(10L);
  //    when(sessionMock.getAttribute("USUARIO")).thenReturn(tecnico);
  //
  //    List<OrdenReparacion> ordenes = Collections.emptyList();
  //
  //    when(servicioOrdenReparacionMock.obtenerOrdenesDelTecnico(10L)).thenReturn(ordenes);
  //
  //    doThrow(new RuntimeException("No se pudo generar el presupuesto"))
  //      .when(servicioPresupuestoMock)
  //      .generarYEnviarPresupuesto(eq(1001), eq(15000.0), eq("Diagnóstico fallido"));
  //
  //    ModelAndView resultado = controladorTecnico.enviarPresupuestoCliente(
  //      1001,
  //      15000.0,
  //      3,
  //      "Diagnóstico fallido",
  //      sessionMock
  //    );
  //
  //    assertThat(resultado.getViewName(), equalTo("panel-tecnico"));
  //
  //    assertThat(
  //      resultado.getModel().get("error"),
  //      equalTo("Error al procesar el presupuesto: No se pudo generar el presupuesto")
  //    );
  //
  //    assertThat(resultado.getModel().get("ordenesAsignadas"), equalTo(ordenes));
  //
  //    verify(servicioPresupuestoMock).generarYEnviarPresupuesto(1001, 15000.0, "Diagnóstico fallido");
  //
  //    verify(servicioOrdenReparacionMock).obtenerOrdenesDelTecnico(10L);
  //
  //    verify(servicioOrdenReparacionMock, never()).obtenerOrdenesParaTecnico();
  //  }

  @Test
  public void mostrarPanelTecnico_sinSesion_deberiaRedirigirAlLogin() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

    ModelAndView resultado = controladorTecnico.mostrarPanelTecnico(sessionMock);

    assertThat(resultado.getViewName(), equalTo("redirect:/login"));

    verify(servicioOrdenReparacionMock, never()).obtenerOrdenesDelTecnico(anyLong());

    verify(servicioOrdenReparacionMock, never()).obtenerOrdenesParaTecnico();
  }

  @Test
  public void enviarPresupuestoCliente_sinSesion_deberiaRedirigirAlLogin() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

    ModelAndView resultado = controladorTecnico.enviarPresupuestoCliente(
      1001,
      15000.0,
      5,
      "Cambio de placa madre",
      sessionMock
    );

    assertThat(resultado.getViewName(), equalTo("redirect:/login"));

    verify(servicioPresupuestoMock, never())
      .generarYEnviarPresupuesto(eq(1001), eq(15000.0), eq("Cambio de placa madre"));

    verify(servicioOrdenReparacionMock, never()).obtenerOrdenesDelTecnico(anyLong());

    verify(servicioOrdenReparacionMock, never()).obtenerOrdenesParaTecnico();
  }

  //Para validar la vista del Historial Tecnico
  @Test
  public void queSePuedaIrALaVistaDeHistorialDesdeElPanelTecnico() {
    //Preparacion para evaluar el historial del tecnico en sesion
    Usuario tecnicoMock = new Usuario();
    tecnicoMock.setId(1L);
    tecnicoMock.setRol("TECNICO");
    HttpSession sessionMock = mock(HttpSession.class);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(tecnicoMock);

    ModelAndView mav = controladorTecnico.verHistorialOrdenesCompletadas(sessionMock);

    assertEquals("panel-tecnico", mav.getViewName());
  }

  @Test
  public void queAlPedirElHistorialElModeloTengaLaListaDeOrdenesCompletadasDelTecnico() {
    Usuario tecnicoMock = new Usuario();
    tecnicoMock.setId(1L);
    tecnicoMock.setRol("TECNICO");
    HttpSession sessionMock = mock(HttpSession.class);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(tecnicoMock);

    List<OrdenReparacion> listaOrdenes = new ArrayList<>();
    listaOrdenes.add(new OrdenReparacion());

    when(servicioOrdenReparacionMock.obtenerHistorialDelTecnico(1L)).thenReturn(listaOrdenes);

    ModelAndView mav = controladorTecnico.verHistorialOrdenesCompletadas(sessionMock);

    // Valida que la lista guardada sea exactamente la que mandó el servicio
    assertEquals(listaOrdenes, mav.getModel().get("historial"));
  }
}
