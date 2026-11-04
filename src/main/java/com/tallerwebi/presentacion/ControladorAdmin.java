package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin")
public class ControladorAdmin {

  private static final String ATRIBUTO_USUARIO = "USUARIO";
  private static final String ROL_ADMIN = "ADMIN";
  private static final String REDIRECT_LOGIN = "redirect:/login";
  private static final String DATOS_REGISTRO = "datosRegistro";
  private static final String VISTA_NUEVO_TECNICO = "admin-nuevo-tecnico";
  private static final String ERROR = "error";

  private final ServicioLogin servicioLogin;

  @Autowired
  public ControladorAdmin(ServicioLogin servicioLogin) {
    this.servicioLogin = servicioLogin;
  }

  @GetMapping("/panel")
  public ModelAndView irAPanelAdmin(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);

    if (usuario == null || !ROL_ADMIN.equalsIgnoreCase(usuario.getRol())) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    return new ModelAndView("panel-admin");
  }

  @GetMapping("/tecnicos/nuevo")
  public ModelAndView irANuevoTecnico(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null || !ROL_ADMIN.equalsIgnoreCase(usuario.getRol())) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    Map<String, Object> modelo = new HashMap<>();
    modelo.put(DATOS_REGISTRO, new DatosRegistro());
    return new ModelAndView(VISTA_NUEVO_TECNICO, modelo);
  }

  @PostMapping("/tecnicos/guardar")
  public ModelAndView guardarTecnico(
    @ModelAttribute(DATOS_REGISTRO) DatosRegistro datosRegistro,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null || !ROL_ADMIN.equalsIgnoreCase(usuario.getRol())) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    Map<String, Object> modelo = new HashMap<>();

    try {
      Usuario nuevoTecnico = new Usuario();
      nuevoTecnico.setEmail(datosRegistro.getEmail());
      nuevoTecnico.setPassword(datosRegistro.getPassword());
      nuevoTecnico.setNombre(datosRegistro.getNombre());
      nuevoTecnico.setApellido(datosRegistro.getApellido());

      // Forzamos el rol desde el backend por seguridad
      nuevoTecnico.setRol("TECNICO");
      nuevoTecnico.setActivo(true);

      servicioLogin.registrar(nuevoTecnico);
    } catch (UsuarioExistente e) {
      modelo.put(ERROR, "El email ya está registrado en el sistema.");
      modelo.put(DATOS_REGISTRO, datosRegistro);
      return new ModelAndView(VISTA_NUEVO_TECNICO, modelo);
    } catch (Exception e) {
      modelo.put(ERROR, "Ocurrió un error al registrar al técnico.");
      modelo.put(DATOS_REGISTRO, datosRegistro);
      return new ModelAndView(VISTA_NUEVO_TECNICO, modelo);
    }

    return new ModelAndView("redirect:/admin/panel?exito=tecnico_creado");
  }

  @GetMapping("/tecnicos")
  public ModelAndView listarTecnicos(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null || !ROL_ADMIN.equalsIgnoreCase(usuario.getRol())) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    Map<String, Object> modelo = new HashMap<>();

    // Asumiendo que tenés un metodo así en tu servicio para traer solo técnicos
    List<Usuario> tecnicos = servicioLogin.obtenerUsuariosPorRol("TECNICO");

    modelo.put("tecnicos", tecnicos);
    return new ModelAndView("admin-tecnicos", modelo);
  }

  @PostMapping("/tecnicos/eliminar/{id}")
  public ModelAndView eliminarTecnico(@PathVariable("id") Long id, HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null || !ROL_ADMIN.equalsIgnoreCase(usuario.getRol())) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    try {
      servicioLogin.eliminarUsuario(id);
      return new ModelAndView("redirect:/admin/tecnicos?exito=eliminado");
    } catch (IllegalStateException e) {
      // Atrapamos la excepción del servicio y devolvemos el error a la vista
      return new ModelAndView("redirect:/admin/tecnicos?error=con_pendientes");
    }
  }
}
