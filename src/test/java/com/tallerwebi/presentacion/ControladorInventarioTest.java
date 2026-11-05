package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Repuesto;
import com.tallerwebi.dominio.ServicioInventario;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorInventarioTest {

  private ServicioInventario servicioInventarioMock;
  private ControladorInventario controladorInventario;
  private HttpSession sessionMock;
  private Usuario usuarioMock;

  private static final String ATRIBUTO_USUARIO = "USUARIO";
  private static final String ROL_ADMIN = "ADMIN";
  private static final String ROL_TECNICO = "TECNICO";

  @BeforeEach
  public void init() {
    servicioInventarioMock = mock(ServicioInventario.class);
    controladorInventario = new ControladorInventario(servicioInventarioMock);
    sessionMock = mock(HttpSession.class);
    usuarioMock = mock(Usuario.class);
  }

  // ==========================================
  // TESTS DE LECTURA (ADMIN Y TÉCNICO)
  // ==========================================

  @Test
  public void queAlVerInventarioComoAdminDevuelvaLaVistaConBanderaEsAdminEnTrue() {
    List<Repuesto> repuestos = new ArrayList<>();
    Integer codigoSeguimiento = 100001;

    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);
    when(servicioInventarioMock.listarTodos()).thenReturn(repuestos);

    ModelAndView mav = controladorInventario.mostrarInventario(codigoSeguimiento, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("inventario"));
    assertThat(mav.getModel().get("repuestos"), notNullValue());
    assertThat(mav.getModel().get("esAdmin"), equalTo(true));
  }

  @Test
  public void queAlVerInventarioComoTecnicoDevuelvaLaVistaConBanderaEsAdminEnFalse() {
    List<Repuesto> repuestos = new ArrayList<>();
    Integer codigoSeguimiento = 100001;

    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_TECNICO);
    when(servicioInventarioMock.listarTodos()).thenReturn(repuestos);

    ModelAndView mav = controladorInventario.mostrarInventario(codigoSeguimiento, sessionMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("inventario"));
    assertThat(mav.getModel().get("esAdmin"), equalTo(false));
  }

  // ==========================================
  // TESTS DE ESCRITURA (SOLO ADMIN)
  // ==========================================

  @Test
  public void queAlGuardarRepuestoComoAdminInvoqueAlServicioYRedirijaAInventario() {
    Repuesto repuesto = new Repuesto();
    repuesto.setNombre("SSD NVMe 500GB");
    Integer codigoSeguimiento = 100001;

    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);

    ModelAndView mav = controladorInventario.guardarRepuesto(
      repuesto,
      codigoSeguimiento,
      sessionMock
    );

    verify(servicioInventarioMock, times(1)).guardarOActualizar(repuesto);
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/inventario?codigo=100001"));
  }

  @Test
  public void queAlEditarRepuestoComoAdminRetorneLaVistaConElObjetoCargado() {
    Long repuestoId = 10L;
    Integer codigoSeguimiento = 100001;
    Repuesto repuesto = new Repuesto();
    repuesto.setId(repuestoId);

    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);
    when(servicioInventarioMock.buscarPorId(repuestoId)).thenReturn(repuesto);
    when(servicioInventarioMock.listarTodos()).thenReturn(new ArrayList<>());

    ModelAndView mav = controladorInventario.editarRepuesto(
      repuestoId,
      codigoSeguimiento,
      sessionMock
    );

    assertThat(mav.getViewName(), equalToIgnoringCase("inventario"));
    assertThat(mav.getModel().get("nuevoRepuesto"), notNullValue());
    verify(servicioInventarioMock, times(1)).buscarPorId(repuestoId);
  }

  @Test
  public void queAlEliminarRepuestoComoAdminInvoqueAlServicioYRedirijaAInventario() {
    Long repuestoId = 5L;

    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);

    ModelAndView mav = controladorInventario.eliminarRepuesto(repuestoId, sessionMock);

    verify(servicioInventarioMock, times(1)).eliminar(repuestoId);
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/inventario"));
  }

  @Test
  public void queAlGuardarRepuestoSinCodigoComoAdminRedirijaAInventarioGeneral() {
    Repuesto repuesto = new Repuesto();
    repuesto.setNombre("SSD NVMe 500GB");
    Integer codigoSeguimiento = null;

    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_ADMIN);

    ModelAndView mav = controladorInventario.guardarRepuesto(
      repuesto,
      codigoSeguimiento,
      sessionMock
    );

    verify(servicioInventarioMock, times(1)).guardarOActualizar(repuesto);
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/inventario"));
  }

  // ==========================================
  // TESTS DE SEGURIDAD (RECHAZO A TÉCNICOS)
  // ==========================================

  @Test
  public void queSiUnTecnicoIntentaGuardarRepuestoSeRechaceYRedirijaALogin() {
    Repuesto repuesto = new Repuesto();
    Integer codigoSeguimiento = 100001;

    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_TECNICO);

    ModelAndView mav = controladorInventario.guardarRepuesto(
      repuesto,
      codigoSeguimiento,
      sessionMock
    );

    verify(servicioInventarioMock, never()).guardarOActualizar(any(Repuesto.class));
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void queSiUnTecnicoIntentaEditarRepuestoSeRechaceYRedirijaALogin() {
    Long repuestoId = 10L;
    Integer codigoSeguimiento = 100001;

    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_TECNICO);

    ModelAndView mav = controladorInventario.editarRepuesto(
      repuestoId,
      codigoSeguimiento,
      sessionMock
    );

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void queSiUnTecnicoIntentaEliminarRepuestoSeRechaceYRedirijaALogin() {
    Long repuestoId = 5L;

    when(sessionMock.getAttribute(ATRIBUTO_USUARIO)).thenReturn(usuarioMock);
    when(usuarioMock.getRol()).thenReturn(ROL_TECNICO);

    ModelAndView mav = controladorInventario.eliminarRepuesto(repuestoId, sessionMock);

    verify(servicioInventarioMock, never()).eliminar(anyLong());
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }
}
