package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
  static final String ATRIBUTO_DATOS_REGISTRO = "datosRegistro";

  private final ServicioLogin servicioLogin;
  private final ServicioOrdenReparacion servicioOrdenReparacion;

  @Autowired
  public ControladorAdmin(
    ServicioLogin servicioLogin,
    ServicioOrdenReparacion servicioOrdenReparacion,
    RepositorioUsuario repositorioUsuario
  ) {
    this.servicioLogin = servicioLogin;
    this.servicioOrdenReparacion = servicioOrdenReparacion;
  }

  public ControladorAdmin(
    ServicioLogin servicioLoginMock,
    ServicioLogin servicioLogin,
    ServicioOrdenReparacion servicioOrdenReparacion,
    RepositorioUsuario repositorioUsuario
  ) {
    this.servicioLogin = servicioLogin;
    this.servicioOrdenReparacion = servicioOrdenReparacion;
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

      nuevoTecnico.setNivelSoporte(datosRegistro.getNivelSoporte());

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

  @GetMapping("/contingencia")
  public ModelAndView irAModuloContingencia(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null || !ROL_ADMIN.equalsIgnoreCase(usuario.getRol())) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    Map<String, Object> modelo = new HashMap<>();

    // Traemos todas las órdenes y filtramos solo las que están activas (no entregadas)
    List<OrdenReparacion> ordenesActivas = servicioOrdenReparacion
      .listarTodas()
      .stream()
      .filter(o -> o.getEstado() != EstadoOrden.ENTREGADO)
      .collect(Collectors.toList());

    // Traemos a los técnicos disponibles para el select
    List<Usuario> tecnicosActivos = servicioLogin.obtenerTecnicosActivos();

    modelo.put("ordenes", ordenesActivas);
    modelo.put("tecnicos", tecnicosActivos);

    return new ModelAndView("admin-contingencia", modelo);
  }

  @PostMapping("/contingencia/reasignar")
  public ModelAndView procesarReasignacion(
    @RequestParam("idOrden") Long idOrden,
    @RequestParam("idTecnico") Long idTecnico,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null || !ROL_ADMIN.equalsIgnoreCase(usuario.getRol())) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    try {
      servicioOrdenReparacion.reasignarOrden(idOrden, idTecnico);
      return new ModelAndView("redirect:/admin/contingencia?exito=reasignado");
    } catch (Exception e) {
      return new ModelAndView("redirect:/admin/contingencia?error=fallo");
    }
  }

  @GetMapping("/tecnicos/editar/{id}")
  public ModelAndView irAEditarTecnico(@PathVariable("id") Long id, HttpSession session) {
    Usuario admin = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (admin == null || !ROL_ADMIN.equalsIgnoreCase(admin.getRol())) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    Usuario tecnico = servicioLogin.buscarUsuarioPorId(id);
    if (tecnico == null) {
      return new ModelAndView("redirect:/admin/tecnicos?error=no_encontrado");
    }

    // Pasamos los datos del usuario al DTO del formulario
    DatosRegistro datos = new DatosRegistro();
    datos.setNombre(tecnico.getNombre());
    datos.setApellido(tecnico.getApellido());
    datos.setEmail(tecnico.getEmail());
    datos.setNivelSoporte(tecnico.getNivelSoporte());

    Map<String, Object> modelo = new HashMap<>();
    modelo.put(ATRIBUTO_DATOS_REGISTRO, datos);
    modelo.put("idTecnico", id);
    return new ModelAndView("admin-editar-tecnico", modelo);
  }

  @PostMapping("/tecnicos/editar/{id}")
  public ModelAndView actualizarTecnico(
    @PathVariable("id") Long id,
    @ModelAttribute(ATRIBUTO_DATOS_REGISTRO) DatosRegistro datos,
    HttpSession session
  ) {
    Usuario admin = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (admin == null || !ROL_ADMIN.equalsIgnoreCase(admin.getRol())) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    try {
      servicioLogin.actualizarDatosTecnico(id, datos);
      return new ModelAndView("redirect:/admin/tecnicos?exito=actualizado");
    } catch (Exception e) {
      Map<String, Object> modelo = new HashMap<>();
      modelo.put("error", "Error al actualizar el técnico.");
      modelo.put(ATRIBUTO_DATOS_REGISTRO, datos);
      modelo.put("idTecnico", id);
      return new ModelAndView("admin-editar-tecnico", modelo);
    }
  }
}
