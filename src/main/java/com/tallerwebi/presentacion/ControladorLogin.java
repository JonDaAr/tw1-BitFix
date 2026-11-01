package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLogin {

  private static final String ROL_TECNICO = "TECNICO";
  private ServicioLogin servicioLogin;
  private final ServicioOrdenReparacion servicioOrdenReparacion;
  private static final String ERROR = "error";
  private static final String VISTA_NUEVO_USUARIO = "nuevo-usuario";

  @Autowired
  public ControladorLogin(
    ServicioLogin servicioLogin,
    ServicioOrdenReparacion servicioOrdenReparacion
  ) {
    this.servicioLogin = servicioLogin;
    this.servicioOrdenReparacion = servicioOrdenReparacion;
  }

  @RequestMapping("/login")
  public ModelAndView irALogin() {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("datosLogin", new DatosLogin());
    return new ModelAndView("login", modelo);
  }

  @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
  public ModelAndView validarLogin(
    @ModelAttribute("datosLogin") DatosLogin datosLogin,
    HttpServletRequest request
  ) {
    Usuario usuarioBuscado = servicioLogin.consultarUsuario(
      datosLogin.getEmail(),
      datosLogin.getPassword()
    );
    if (usuarioBuscado != null) {
      request.getSession().setAttribute("USUARIO", usuarioBuscado);
      request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
      String rol = usuarioBuscado.getRol();
      if (ROL_TECNICO.equalsIgnoreCase(rol)) {
        return new ModelAndView("redirect:/tecnico/panel-tecnico");
      } else {
        return new ModelAndView("redirect:/home");
      }
    } else {
      Map<String, Object> model = new ModelMap();
      model.put("error", "Usuario o clave incorrecta");
      return new ModelAndView("login", model);
    }
  }

  @GetMapping("/nuevo-usuario")
  public ModelAndView nuevoUsuario() {
    Map<String, Object> modelo = new HashMap<>(); // Usamos interfaz Map y HashMap
    modelo.put("datosRegistro", new DatosRegistro());
    return new ModelAndView(VISTA_NUEVO_USUARIO, modelo);
  }

  @PostMapping("/guardar-usuario")
  public ModelAndView registrarUsuario(
    @ModelAttribute("datosRegistro") DatosRegistro datosRegistro
  ) {
    Map<String, Object> modelo = new HashMap<>();

    // 1. Validar que las contraseñas coincidan
    if (!datosRegistro.getPassword().equals(datosRegistro.getRepetirPassword())) {
      modelo.put(ERROR, "Las contraseñas no coinciden");
      return new ModelAndView(VISTA_NUEVO_USUARIO, modelo);
    }

    try {
      // 2. Mapear DTO a Entidad
      Usuario nuevoUsuario = new Usuario();
      nuevoUsuario.setEmail(datosRegistro.getEmail());
      nuevoUsuario.setPassword(datosRegistro.getPassword());
      nuevoUsuario.setNombre(datosRegistro.getNombre());
      nuevoUsuario.setApellido(datosRegistro.getApellido());
      nuevoUsuario.setTelefono(datosRegistro.getTelefono());
      nuevoUsuario.setNombreUsuario(datosRegistro.getNombreUsuario());
      nuevoUsuario.setRol("CLIENTE");
      nuevoUsuario.setActivo(true);

      // 3. Guardar
      servicioLogin.registrar(nuevoUsuario);
    } catch (UsuarioExistente e) {
      modelo.put(ERROR, "El email ya está registrado");
      return new ModelAndView(VISTA_NUEVO_USUARIO, modelo);
    } catch (Exception e) {
      modelo.put(ERROR, "Error al registrar el usuario");
      return new ModelAndView(VISTA_NUEVO_USUARIO, modelo);
    }

    return new ModelAndView("redirect:/login");
  }

  @RequestMapping(path = "/home", method = RequestMethod.GET)
  public ModelAndView irAHome(HttpSession session) {
    Map<String, Object> model = new ModelMap();

    Usuario usuario = (Usuario) session.getAttribute("USUARIO");

    List<OrdenReparacion> ordenes = servicioOrdenReparacion.obtenerOrdenesDelCliente(
      usuario.getEmail()
    );

    model.put("ordenes", ordenes);

    return new ModelAndView("home", model);
  }

  @RequestMapping(path = "/", method = RequestMethod.GET)
  public ModelAndView inicio() {
    return new ModelAndView("redirect:/login");
  }
}
