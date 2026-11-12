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

@Controller
public class ControladorCanje {

  private static final String USUARIO = "USUARIO";
  private static final String REDIRECT_LOGIN = "redirect:/login";
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
    HttpSession session
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
      return new ModelAndView("redirect:/maquinas?error=datos");
    }
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
    HttpSession session
  ) {
    if (!esAdmin((Usuario) session.getAttribute(USUARIO))) return new ModelAndView(REDIRECT_LOGIN);
    try {
      servicioCanje.guardarComputadora(computadora);
      return new ModelAndView("redirect:/admin/maquinas?guardada=si");
    } catch (IllegalArgumentException e) {
      return new ModelAndView("redirect:/admin/maquinas?error=datos");
    }
  }

  @PostMapping("/admin/maquinas/{id}/pausar")
  public ModelAndView pausar(@PathVariable("id") Long id, HttpSession session) {
    if (!esAdmin((Usuario) session.getAttribute(USUARIO))) return new ModelAndView(REDIRECT_LOGIN);
    try {
      servicioCanje.pausarComputadora(id);
      return new ModelAndView("redirect:/admin/maquinas?guardada=si");
    } catch (IllegalArgumentException e) {
      return new ModelAndView("redirect:/admin/maquinas?error=datos");
    }
  }

  @PostMapping("/admin/maquinas/solicitudes/{id}/resolver")
  public ModelAndView resolver(
    @PathVariable("id") Long id,
    @RequestParam("aprobar") boolean aprobar,
    @RequestParam(value = "valorAprobado", required = false) Double valorAprobado,
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
