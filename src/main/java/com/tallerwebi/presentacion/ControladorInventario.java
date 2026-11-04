package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Repuesto;
import com.tallerwebi.dominio.ServicioInventario;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorInventario {

  private final ServicioInventario servicioInventario;

  private static final String ATRIBUTO_USUARIO = "USUARIO";
  private static final String ROL_ADMIN = "ADMIN";
  private static final String REDIRECT_LOGIN = "redirect:/login";
  private static final String REDIRECT_INVENTARIO = "redirect:/inventario";
  private static final String VISTA_INVENTARIO = "inventario";

  @Autowired
  public ControladorInventario(ServicioInventario servicioInventario) {
    this.servicioInventario = servicioInventario;
  }

  @GetMapping("/inventario")
  public ModelAndView mostrarInventario(
    @RequestParam(value = "codigo", required = false) Integer codigo,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    List<Repuesto> repuestos = servicioInventario.listarTodos();
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("repuestos", repuestos);

    // Inyectamos un objeto vacío para el formulario de alta
    modelo.put("nuevoRepuesto", new Repuesto());
    modelo.put("codigoSeguimiento", codigo);
    modelo.put("esAdmin", ROL_ADMIN.equalsIgnoreCase(usuario.getRol()));

    return new ModelAndView(VISTA_INVENTARIO, modelo);
  }

  @PostMapping("/inventario/guardar")
  public ModelAndView guardarRepuesto(
    @ModelAttribute("repuesto") Repuesto repuesto,
    @RequestParam(value = "codigo", required = false) Integer codigo,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);

    if (usuario == null || !ROL_ADMIN.equalsIgnoreCase(usuario.getRol())) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    servicioInventario.guardarOActualizar(repuesto);

    if (codigo != null) {
      return new ModelAndView(REDIRECT_INVENTARIO + "?codigo=" + codigo);
    }
    return new ModelAndView(REDIRECT_INVENTARIO);
  }

  @GetMapping("/inventario/editar/{id}")
  public ModelAndView editarRepuesto(
    @PathVariable("id") Long id,
    @RequestParam(value = "codigo", required = false) Integer codigo,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);

    if (usuario == null || !ROL_ADMIN.equalsIgnoreCase(usuario.getRol())) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    Repuesto repuesto = servicioInventario.buscarPorId(id);

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("repuestos", servicioInventario.listarTodos());
    modelo.put("nuevoRepuesto", repuesto);
    modelo.put("codigoSeguimiento", codigo);
    modelo.put("esAdmin", true);

    return new ModelAndView(VISTA_INVENTARIO, modelo);
  }

  @PostMapping("/inventario/eliminar/{id}")
  public ModelAndView eliminarRepuesto(@PathVariable("id") Long id, HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);

    if (usuario == null || !ROL_ADMIN.equalsIgnoreCase(usuario.getRol())) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    servicioInventario.eliminar(id);
    return new ModelAndView(REDIRECT_INVENTARIO);
  }
}
