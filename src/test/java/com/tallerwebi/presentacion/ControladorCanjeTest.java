package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.ComputadoraUsada;
import com.tallerwebi.dominio.ServicioCanje;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

public class ControladorCanjeTest {

  private ServicioCanje servicio;
  private ControladorCanje controlador;
  private HttpSession session;
  private Usuario cliente;

  @BeforeEach
  public void init() {
    servicio = mock(ServicioCanje.class);
    controlador = new ControladorCanje(servicio);
    session = mock(HttpSession.class);
    cliente = usuario("CLIENTE");
  }

  private Usuario crearAdminEnSesion() {
    Usuario admin = new Usuario();
    admin.setId(1L);
    admin.setRol("ADMIN");
    when(session.getAttribute("USUARIO")).thenReturn(admin);
    return admin;
  }

  private Usuario crearClienteEnSesion() {
    Usuario cliente = new Usuario();
    cliente.setId(2L);
    cliente.setRol("CLIENTE");
    when(session.getAttribute("USUARIO")).thenReturn(cliente);
    return cliente;
  }

  @Test
  public void catalogoSoloPermiteClientesYEntregaSusDatos() {
    assertEquals("redirect:/login", controlador.catalogo(session).getViewName());
    when(session.getAttribute("USUARIO")).thenReturn(usuario("TECNICO"));
    assertEquals("redirect:/login", controlador.catalogo(session).getViewName());

    when(session.getAttribute("USUARIO")).thenReturn(cliente);
    when(servicio.listarDisponibles()).thenReturn(List.of(new ComputadoraUsada()));
    when(servicio.listarSolicitudesDeCliente(8L)).thenReturn(List.of());

    ModelAndView resultado = controlador.catalogo(session);

    assertEquals("maquinas-usadas", resultado.getViewName());
    verify(servicio).listarSolicitudesDeCliente(8L);
  }

  @Test
  public void solicitarCanjeControlaRolYResultadoDelServicio() {
    assertEquals(
      "redirect:/login",
      controlador.solicitar(2L, "Dell", "Latitude", "Falla", null, session).getViewName()
    );

    when(session.getAttribute("USUARIO")).thenReturn(cliente);
    assertEquals(
      "redirect:/maquinas?solicitud=creada",
      controlador.solicitar(2L, "Dell", "Latitude", "Falla", 1000.0, session).getViewName()
    );
    verify(servicio).solicitarCanje(2L, cliente, "Dell", "Latitude", "Falla", 1000.0);
  }

  @Test
  public void solicitarCanjeRedirigeSiElServicioRechazaLosDatos() {
    when(session.getAttribute("USUARIO")).thenReturn(cliente);
    doThrow(new IllegalArgumentException())
      .when(servicio)
      .solicitarCanje(2L, cliente, "", "Latitude", "Falla", null);

    ModelAndView resultado = controlador.solicitar(2L, "", "Latitude", "Falla", null, session);

    assertEquals("redirect:/maquinas", resultado.getViewName());
  }

  @Test
  public void administrarSoloPermiteAdministradores() {
    assertEquals("redirect:/login", controlador.administrar(session).getViewName());
    when(session.getAttribute("USUARIO")).thenReturn(usuario("ADMIN"));
    when(servicio.listarTodasLasComputadoras()).thenReturn(List.of());
    when(servicio.listarSolicitudes()).thenReturn(List.of());

    ModelAndView resultado = controlador.administrar(session);

    assertEquals("admin-maquinas", resultado.getViewName());
  }

  @Test
  public void guardarComputadoraControlaRolYResultado() {
    ComputadoraUsada computadora = new ComputadoraUsada();
    assertEquals("redirect:/login", controlador.guardar(computadora, session).getViewName());

    when(session.getAttribute("USUARIO")).thenReturn(usuario("ADMIN"));
    assertEquals(
      "redirect:/admin/maquinas?guardada=si",
      controlador.guardar(computadora, session).getViewName()
    );

    doThrow(new IllegalArgumentException()).when(servicio).guardarComputadora(computadora);
    assertEquals(
      "redirect:/admin/maquinas",
      controlador.guardar(computadora, session).getViewName()
    );
  }

  @Test
  public void pausarComputadoraControlaRolYResultado() {
    assertEquals("redirect:/login", controlador.pausar(3L, session).getViewName());
    when(session.getAttribute("USUARIO")).thenReturn(usuario("ADMIN"));
    assertEquals(
      "redirect:/admin/maquinas?guardada=si",
      controlador.pausar(3L, session).getViewName()
    );

    doThrow(new IllegalArgumentException()).when(servicio).pausarComputadora(3L);
    assertEquals("redirect:/admin/maquinas", controlador.pausar(3L, session).getViewName());
  }

  @Test
  public void resolverSolicitudControlaRolYResultado() {
    assertEquals("redirect:/login", controlador.resolver(4L, true, 1000.0, session).getViewName());
    when(session.getAttribute("USUARIO")).thenReturn(usuario("ADMIN"));
    assertEquals(
      "redirect:/admin/maquinas?resuelta=si",
      controlador.resolver(4L, true, 1000.0, session).getViewName()
    );

    doThrow(new IllegalStateException()).when(servicio).resolverSolicitud(4L, true, 1000.0);
    assertEquals(
      "redirect:/admin/maquinas?error=solicitud",
      controlador.resolver(4L, true, 1000.0, session).getViewName()
    );
  }

  private Usuario usuario(String rol) {
    Usuario usuario = new Usuario();
    usuario.setId(8L);
    usuario.setRol(rol);
    return usuario;
  }

  @Test
  public void reactivarYRetirarComputadoraControlanRolYResultado() {
    RedirectAttributes flash = new RedirectAttributesModelMap();

    assertEquals("redirect:/login", controlador.reactivar(3L, session, flash).getViewName());
    assertEquals("redirect:/login", controlador.retirar(3L, session, flash).getViewName());

    when(session.getAttribute("USUARIO")).thenReturn(usuario("ADMIN"));
    assertEquals(
      "redirect:/admin/maquinas?guardada=si",
      controlador.reactivar(3L, session, flash).getViewName()
    );
    assertEquals(
      "redirect:/admin/maquinas?guardada=si",
      controlador.retirar(3L, session, flash).getViewName()
    );

    doThrow(new IllegalArgumentException("Error reactivar"))
      .when(servicio)
      .reactivarComputadora(3L);
    doThrow(new IllegalArgumentException("Error retirar")).when(servicio).retirarComputadora(3L);

    assertEquals(
      "redirect:/admin/maquinas",
      controlador.reactivar(3L, session, flash).getViewName()
    );
    assertEquals("Error reactivar", flash.getFlashAttributes().get("errorMensaje"));

    assertEquals("redirect:/admin/maquinas", controlador.retirar(3L, session, flash).getViewName());
    assertEquals("Error retirar", flash.getFlashAttributes().get("errorMensaje"));
  }

  @Test
  public void resolverSolicitudAdminControlaRolAccionesYErrores() {
    RedirectAttributes flash = new RedirectAttributesModelMap();

    assertEquals(
      "redirect:/login",
      controlador.resolverSolicitud(4L, "APROBAR", null, null, session, flash).getViewName()
    );

    when(session.getAttribute("USUARIO")).thenReturn(usuario("ADMIN"));
    assertEquals(
      "redirect:/admin/maquinas?resuelta=si",
      controlador.resolverSolicitud(4L, "APROBAR", 1000.0, "OK", session, flash).getViewName()
    );
    verify(servicio).responderSolicitudAdmin(4L, false, true, 1000.0, "OK");

    controlador.resolverSolicitud(5L, "CONTRAOFERTAR", 800.0, "Oferta", session, flash);
    verify(servicio).responderSolicitudAdmin(5L, true, false, 800.0, "Oferta");

    controlador.resolverSolicitud(6L, "RECHAZAR", null, "No", session, flash);
    verify(servicio).responderSolicitudAdmin(6L, false, false, null, "No");

    assertEquals(
      "redirect:/admin/maquinas",
      controlador.resolverSolicitud(4L, "INVALIDA", 500.0, "Nota", session, flash).getViewName()
    );
    assertEquals(
      "La acción seleccionada no es válida.",
      flash.getFlashAttributes().get("errorMensaje")
    );
  }

  @Test
  public void responderContraofertaControlaRolYResultado() {
    RedirectAttributes flash = new RedirectAttributesModelMap();

    assertEquals(
      "redirect:/login",
      controlador.responderContraoferta(7L, true, session, flash).getViewName()
    );

    when(session.getAttribute("USUARIO")).thenReturn(cliente);
    assertEquals(
      "redirect:/maquinas?solicitud=respondida",
      controlador.responderContraoferta(7L, true, session, flash).getViewName()
    );
    verify(servicio).responderContraoferta(7L, 8L, true);

    doThrow(new IllegalStateException("Sin contraoferta"))
      .when(servicio)
      .responderContraoferta(7L, 8L, false);

    assertEquals(
      "redirect:/maquinas",
      controlador.responderContraoferta(7L, false, session, flash).getViewName()
    );
    assertEquals("Sin contraoferta", flash.getFlashAttributes().get("errorMensaje"));
  }
}
