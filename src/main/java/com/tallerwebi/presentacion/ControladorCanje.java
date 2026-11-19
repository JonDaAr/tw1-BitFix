package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ComputadoraUsada;
import com.tallerwebi.dominio.ServicioCanje;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

@Controller
public class ControladorCanje {

  private static final String USUARIO = "USUARIO";
  private static final String REDIRECT_LOGIN = "redirect:/login";
  private static final String ERROR_FLASH = "errorMensaje";
  private static final String REDIRECT_ADMIN_MAQUINAS = "redirect:/admin/maquinas";
  private static final String REDIRECT_ADMIN_GUARDADA = "redirect:/admin/maquinas?guardada=si";
  private final ServicioCanje servicioCanje;

  @Autowired
  public ControladorCanje(ServicioCanje servicioCanje) {
    this.servicioCanje = servicioCanje;
  }

  @GetMapping("/maquinas")
  public ModelAndView catalogo(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO);
    if (!esCliente(usuario)) return new ModelAndView(REDIRECT_LOGIN);
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("computadoras", servicioCanje.listarDisponibles());
    modelo.put("solicitudes", servicioCanje.listarSolicitudesDeCliente(usuario.getId()));
    return new ModelAndView("maquinas-usadas", modelo);
  }

  @PostMapping("/plan-canje/solicitar")
  public ModelAndView solicitar(
    @RequestParam("computadoraId") Long computadoraId,
    @RequestParam("equipoMarca") String equipoMarca,
    @RequestParam("equipoModelo") String equipoModelo,
    @RequestParam("equipoDescripcion") String equipoDescripcion,
    @RequestParam(value = "valorOfrecido", required = false) Double valorOfrecido,
    HttpSession session,
    RedirectAttributes flash
  ) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO);
    if (!esCliente(usuario)) return new ModelAndView(REDIRECT_LOGIN);
    try {
      servicioCanje.solicitarCanje(
        computadoraId,
        usuario,
        equipoMarca,
        equipoModelo,
        equipoDescripcion,
        valorOfrecido
      );
      return new ModelAndView("redirect:/maquinas?solicitud=creada");
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute(ERROR_FLASH, e.getMessage());
      flash.addFlashAttribute("solicitudComputadoraId", computadoraId);
      flash.addFlashAttribute("equipoMarca", equipoMarca);
      flash.addFlashAttribute("equipoModelo", equipoModelo);
      flash.addFlashAttribute("equipoDescripcion", equipoDescripcion);
      flash.addFlashAttribute("valorOfrecido", valorOfrecido);
      return new ModelAndView("redirect:/maquinas");
    }
  }

  public ModelAndView solicitar(
    Long computadoraId,
    String equipoMarca,
    String equipoModelo,
    String equipoDescripcion,
    Double valorOfrecido,
    HttpSession session
  ) {
    return solicitar(
      computadoraId,
      equipoMarca,
      equipoModelo,
      equipoDescripcion,
      valorOfrecido,
      session,
      new RedirectAttributesModelMap()
    );
  }

  @GetMapping("/admin/maquinas")
  public ModelAndView administrar(HttpSession session) {
    if (!esAdmin((Usuario) session.getAttribute(USUARIO))) return new ModelAndView(REDIRECT_LOGIN);
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("computadoras", servicioCanje.listarTodasLasComputadoras());
    modelo.put("solicitudes", servicioCanje.listarSolicitudes());
    modelo.put("nuevaComputadora", new ComputadoraUsada());
    return new ModelAndView("admin-maquinas", modelo);
  }

  @PostMapping("/admin/maquinas/guardar")
  public ModelAndView guardar(
    @ModelAttribute("nuevaComputadora") ComputadoraUsada computadora,
    HttpSession session,
    RedirectAttributes flash
  ) {
    if (!esAdmin((Usuario) session.getAttribute(USUARIO))) return new ModelAndView(REDIRECT_LOGIN);
    try {
      servicioCanje.guardarComputadora(computadora);
      return new ModelAndView(REDIRECT_ADMIN_GUARDADA);
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute(ERROR_FLASH, e.getMessage());
      flash.addFlashAttribute("formComputadora", computadora);
      return new ModelAndView(REDIRECT_ADMIN_MAQUINAS);
    }
  }

  public ModelAndView guardar(ComputadoraUsada computadora, HttpSession session) {
    return guardar(computadora, session, new RedirectAttributesModelMap());
  }

  @PostMapping("/admin/maquinas/{id}/pausar")
  public ModelAndView pausar(
    @PathVariable("id") Long id,
    HttpSession session,
    RedirectAttributes flash
  ) {
    if (!esAdmin((Usuario) session.getAttribute(USUARIO))) return new ModelAndView(REDIRECT_LOGIN);
    try {
      servicioCanje.pausarComputadora(id);
      return new ModelAndView(REDIRECT_ADMIN_GUARDADA);
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute(ERROR_FLASH, e.getMessage());
      return new ModelAndView(REDIRECT_ADMIN_MAQUINAS);
    }
  }

  public ModelAndView pausar(Long id, HttpSession session) {
    return pausar(id, session, new RedirectAttributesModelMap());
  }

  @PostMapping("/admin/maquinas/{id}/reactivar")
  public ModelAndView reactivar(
    @PathVariable("id") Long id,
    HttpSession session,
    RedirectAttributes flash
  ) {
    if (!esAdmin((Usuario) session.getAttribute(USUARIO))) return new ModelAndView(REDIRECT_LOGIN);
    try {
      servicioCanje.reactivarComputadora(id);
      return new ModelAndView(REDIRECT_ADMIN_GUARDADA);
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute(ERROR_FLASH, e.getMessage());
      return new ModelAndView(REDIRECT_ADMIN_MAQUINAS);
    }
  }

  @PostMapping("/admin/maquinas/{id}/retirar")
  public ModelAndView retirar(
    @PathVariable("id") Long id,
    HttpSession session,
    RedirectAttributes flash
  ) {
    if (!esAdmin((Usuario) session.getAttribute(USUARIO))) return new ModelAndView(REDIRECT_LOGIN);
    try {
      servicioCanje.retirarComputadora(id);
      return new ModelAndView(REDIRECT_ADMIN_GUARDADA);
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute(ERROR_FLASH, e.getMessage());
      return new ModelAndView(REDIRECT_ADMIN_MAQUINAS);
    }
  }

  @PostMapping("/admin/maquinas/solicitudes/{id}/resolver")
  public ModelAndView resolverSolicitud(
    @PathVariable("id") Long id,
    @RequestParam("accion") String accion,
    @RequestParam(value = "valor", required = false) Double valor,
    @RequestParam(value = "comentario", required = false) String comentario,
    HttpSession session,
    RedirectAttributes flash
  ) {
    if (!esAdmin((Usuario) session.getAttribute(USUARIO))) return new ModelAndView(REDIRECT_LOGIN);
    try {
      boolean contraofertar = "CONTRAOFERTAR".equals(accion);
      boolean aprobar = "APROBAR".equals(accion);
      if (!contraofertar && !aprobar && !"RECHAZAR".equals(accion)) {
        throw new IllegalArgumentException("La acción seleccionada no es válida.");
      }
      servicioCanje.responderSolicitudAdmin(id, contraofertar, aprobar, valor, comentario);
      return new ModelAndView("redirect:/admin/maquinas?resuelta=si");
    } catch (IllegalArgumentException | IllegalStateException e) {
      flash.addFlashAttribute(ERROR_FLASH, e.getMessage());
      flash.addFlashAttribute("formSolicitudId", id);
      flash.addFlashAttribute("formValor", valor);
      flash.addFlashAttribute("formComentario", comentario);
      return new ModelAndView(REDIRECT_ADMIN_MAQUINAS);
    }
  }

  @PostMapping("/plan-canje/solicitudes/{id}/responder")
  public ModelAndView responderContraoferta(
    @PathVariable("id") Long id,
    @RequestParam("aceptar") boolean aceptar,
    HttpSession session,
    RedirectAttributes flash
  ) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO);
    if (!esCliente(usuario)) return new ModelAndView(REDIRECT_LOGIN);
    try {
      servicioCanje.responderContraoferta(id, usuario.getId(), aceptar);
      return new ModelAndView("redirect:/maquinas?solicitud=respondida");
    } catch (IllegalArgumentException | IllegalStateException e) {
      flash.addFlashAttribute(ERROR_FLASH, e.getMessage());
      return new ModelAndView("redirect:/maquinas");
    }
  }

  public ModelAndView resolver(
    Long id,
    boolean aprobar,
    Double valorAprobado,
    HttpSession session
  ) {
    if (!esAdmin((Usuario) session.getAttribute(USUARIO))) return new ModelAndView(REDIRECT_LOGIN);
    try {
      servicioCanje.resolverSolicitud(id, aprobar, valorAprobado);
      return new ModelAndView("redirect:/admin/maquinas?resuelta=si");
    } catch (IllegalArgumentException | IllegalStateException e) {
      return new ModelAndView("redirect:/admin/maquinas?error=solicitud");
    }
  }

  private boolean esCliente(Usuario usuario) {
    return usuario != null && "CLIENTE".equalsIgnoreCase(usuario.getRol());
  }

  private boolean esAdmin(Usuario usuario) {
    return usuario != null && "ADMIN".equalsIgnoreCase(usuario.getRol());
  }
}
