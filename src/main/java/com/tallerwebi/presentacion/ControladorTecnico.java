package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/tecnico")
public class ControladorTecnico {

  private static final Logger LOGGER = LoggerFactory.getLogger(ControladorTecnico.class);

  private final ServicioOrdenReparacion servicioOrdenReparacion;
  private final ServicioPresupuesto servicioPresupuesto;
  private final RepositorioUsuario repositorioUsuario; // NUEVO: Para crear usuarios desde el local

  // Constantes requeridas por PMD para evitar AvoidDuplicateLiterals
  private static final String ATRIBUTO_USUARIO = "USUARIO";
  private static final String ROL_TECNICO = "TECNICO";
  private static final String ATRIBUTO_VISTA = "vista";
  private static final String VISTA_ORDENES = "ordenes";
  private static final String VISTA_PANEL_TECNICO = "panel-tecnico";

  @Autowired
  public ControladorTecnico(
    ServicioOrdenReparacion servicioOrdenReparacion,
    ServicioPresupuesto servicioPresupuesto,
    RepositorioUsuario repositorioUsuario
  ) {
    this.servicioOrdenReparacion = servicioOrdenReparacion;
    this.servicioPresupuesto = servicioPresupuesto;
    this.repositorioUsuario = repositorioUsuario;
  }

  @GetMapping("/panel-tecnico")
  public ModelAndView mostrarPanelTecnico(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    Map<String, Object> model = new HashMap<>();

    List<OrdenReparacion> ordenesAsignadas = servicioOrdenReparacion.obtenerOrdenesDelTecnico(
      usuario.getId()
    );

    model.put("ordenesAsignadas", ordenesAsignadas);
    model.put(ATRIBUTO_VISTA, VISTA_ORDENES);
    return new ModelAndView(VISTA_PANEL_TECNICO, model);
  }

  @GetMapping("/nueva-orden")
  public ModelAndView nuevaOrden(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);

    // BLOQUEO DE SEGURIDAD: Solo Técnicos Nivel 3 (Tier 3) pueden ingresar equipos físicamente
    if (
      usuario == null ||
      !ROL_TECNICO.equals(usuario.getRol()) ||
      usuario.getNivelSoporte() != NivelReparacion.NIVEL_3
    ) {
      return new ModelAndView("redirect:/tecnico/panel-tecnico");
    }

    Map<String, Object> model = new HashMap<>();
    model.put("datosOrden", new DatosOrden());
    model.put(ATRIBUTO_VISTA, "nueva-orden");

    return new ModelAndView(VISTA_PANEL_TECNICO, model);
  }

  @PostMapping("/recepcion")
  public ModelAndView registrarOrden(DatosOrden datosOrden, HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);

    // BLOQUEO DE SEGURIDAD POST: Evita que inyecten datos por API
    if (
      usuario == null ||
      !ROL_TECNICO.equals(usuario.getRol()) ||
      usuario.getNivelSoporte() != NivelReparacion.NIVEL_3
    ) {
      return new ModelAndView("redirect:/tecnico/panel-tecnico");
    }

    Map<String, Object> model = new HashMap<>();

    try {
      this.servicioOrdenReparacion.registrarOrdenEnLocal(datosOrden);

      // Si el servicio nos devolvió una clave provisoria, es un cliente nuevo
      if (datosOrden.getPasswordProvisoria() != null) {
        model.put("mostrarModalCredenciales", true);
        model.put("modalEmail", datosOrden.getEmailCliente());
        model.put("modalPassword", datosOrden.getPasswordProvisoria());
        model.put("modalTelefono", datosOrden.getTelefonoCliente());
        model.put("exito", "Equipo ingresado. El nuevo cliente fue registrado exitosamente.");
      } else {
        model.put("exito", "La orden ha sido registrada correctamente.");
      }

      List<OrdenReparacion> ordenesAsignadas = servicioOrdenReparacion.obtenerOrdenesDelTecnico(
        usuario.getId()
      );
      model.put("ordenesAsignadas", ordenesAsignadas);
      model.put(ATRIBUTO_VISTA, VISTA_ORDENES);

      return new ModelAndView(VISTA_PANEL_TECNICO, model);
    } catch (Exception e) {
      model.put("error", e.getMessage());
      model.put("datosOrden", datosOrden);
      model.put(ATRIBUTO_VISTA, "nueva-orden");
      return new ModelAndView(VISTA_PANEL_TECNICO, model);
    }
  }

  @GetMapping("/historial")
  public ModelAndView verHistorialOrdenesCompletadas(HttpSession session) {
    Map<String, Object> model = new HashMap<>();
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);

    List<OrdenReparacion> historial = servicioOrdenReparacion.obtenerHistorialDelTecnico(
      usuario.getId()
    );

    model.put("historial", historial);
    model.put(ATRIBUTO_VISTA, "historial-tecnico");
    model.put("tecnico", usuario);

    return new ModelAndView(VISTA_PANEL_TECNICO, model);
  }

  // ==============================================================================
  // BANDEJA DE TRIAJE (EXCLUSIVA PARA TÉCNICOS SENIOR / NIVEL 3)
  // ==============================================================================

  @GetMapping("/triaje")
  public ModelAndView mostrarBandejaTriaje(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);

    if (
      usuario == null ||
      !ROL_TECNICO.equals(usuario.getRol()) ||
      usuario.getNivelSoporte() != NivelReparacion.NIVEL_3
    ) {
      return new ModelAndView("redirect:/login");
    }

    List<OrdenReparacion> ordenesEnTriaje = servicioOrdenReparacion
      .listarTodas()
      .stream()
      .filter(orden -> orden.getEstado() == EstadoOrden.PENDIENTE_TRIAJE)
      .collect(Collectors.toList());

    Map<String, Object> modelo = new HashMap<>();
    modelo.put(VISTA_ORDENES, ordenesEnTriaje);
    modelo.put("nivelesSoporte", NivelReparacion.values());

    return new ModelAndView("triaje-cotizaciones", modelo);
  }

  @PostMapping("/derivar-cotizacion")
  public ModelAndView derivarCotizacion(
    @RequestParam("idOrden") Long idOrden,
    @RequestParam("nivelReparacion") NivelReparacion nivelReparacion,
    @RequestParam("estimadoMinimo") Double estimadoMinimo,
    @RequestParam("estimadoMaximo") Double estimadoMaximo,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);

    if (
      usuario == null ||
      !ROL_TECNICO.equals(usuario.getRol()) ||
      usuario.getNivelSoporte() != NivelReparacion.NIVEL_3
    ) {
      return new ModelAndView("redirect:/login");
    }

    try {
      // Pasamos el rango estimado al servicio junto al nivel
      servicioOrdenReparacion.derivarCotizacion(
        idOrden,
        nivelReparacion,
        estimadoMinimo,
        estimadoMaximo
      );
    } catch (NoHayTecnicosDisponibles e) {
      Map<String, Object> modelo = new HashMap<>();
      modelo.put(
        "error",
        "No hay técnicos disponibles para el nivel seleccionado en este momento."
      );

      List<OrdenReparacion> ordenesEnTriaje = servicioOrdenReparacion
        .listarTodas()
        .stream()
        .filter(o -> o.getEstado() == EstadoOrden.PENDIENTE_TRIAJE)
        .collect(Collectors.toList());

      modelo.put(VISTA_ORDENES, ordenesEnTriaje);
      modelo.put("nivelesSoporte", NivelReparacion.values());

      return new ModelAndView("triaje-cotizaciones", modelo);
    }

    return new ModelAndView("redirect:/tecnico/triaje");
  }

  @GetMapping("/esperados")
  public ModelAndView mostrarEquiposEsperados(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null || !ROL_TECNICO.equals(usuario.getRol())) {
      return new ModelAndView("redirect:/login");
    }

    Map<String, Object> model = new HashMap<>();
    List<OrdenReparacion> esperadas = servicioOrdenReparacion.obtenerOrdenesEsperadasDelTecnico(
      usuario.getId()
    );

    model.put("ordenesEsperadas", esperadas);
    model.put(ATRIBUTO_VISTA, "esperados"); // Activa el nuevo tab en el HTML
    return new ModelAndView(VISTA_PANEL_TECNICO, model);
  }

  @PostMapping("/recibir-equipo")
  public ModelAndView recibirEquipoFisico(
    @RequestParam("idOrden") Long idOrden,
    HttpSession session
  ) {
    // Pasa el estado a EN_DIAGNOSTICO y guarda la nota en el historial
    servicioOrdenReparacion.actualizarEstadoYNotaTecnica(
      idOrden,
      EstadoOrden.EN_DIAGNOSTICO,
      "El equipo ingresó físicamente al taller y está listo para diagnóstico."
    );

    Map<String, Object> model = new HashMap<>();
    model.put(
      "mensaje",
      "¡Equipo recibido con éxito! Ya se encuentra en tu panel de trabajo principal."
    );
    model.put(ATRIBUTO_VISTA, "esperados");

    // Recargamos la lista
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    model.put(
      "ordenesEsperadas",
      servicioOrdenReparacion.obtenerOrdenesEsperadasDelTecnico(usuario.getId())
    );

    return new ModelAndView(VISTA_PANEL_TECNICO, model);
  }
}
