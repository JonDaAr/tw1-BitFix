package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

  private final ServicioOrdenReparacion servicioOrdenReparacion;
  private final ServicioPresupuesto servicioPresupuesto;

  // Constantes requeridas por PMD para evitar AvoidDuplicateLiterals
  private static final String ATRIBUTO_USUARIO = "USUARIO";
  private static final String ROL_TECNICO = "TECNICO";
  private static final String ATRIBUTO_VISTA = "vista";
  private static final String VISTA_ORDENES = "ordenes";
  private static final String VISTA_PANEL_TECNICO = "panel-tecnico";

  @Autowired
  public ControladorTecnico(
    ServicioOrdenReparacion servicioOrdenReparacion,
    ServicioPresupuesto servicioPresupuesto
  ) {
    this.servicioOrdenReparacion = servicioOrdenReparacion;
    this.servicioPresupuesto = servicioPresupuesto;
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
  public ModelAndView nuevaOrden() {
    Map<String, Object> model = new HashMap<>();

    model.put("datosOrden", new DatosOrden());
    model.put(ATRIBUTO_VISTA, "nueva-orden");

    return new ModelAndView(VISTA_PANEL_TECNICO, model);
  }

  @PostMapping("/recepcion")
  public ModelAndView registrarOrden(DatosOrden datosOrden, HttpSession session) {
    OrdenReparacion nuevaOrden = new OrdenReparacion();
    nuevaOrden.setNombreCliente(datosOrden.getNombreCliente());
    nuevaOrden.setEmailCliente(datosOrden.getEmailCliente());
    nuevaOrden.setModeloEquipo(datosOrden.getModeloEquipo());
    nuevaOrden.setDescripcionFalla(datosOrden.getDescripcionFalla());
    nuevaOrden.setNivelReparacion(datosOrden.getNivelReparacion());

    Map<String, Object> model = new HashMap<>();

    try {
      this.servicioOrdenReparacion.registrarOrden(nuevaOrden);

      model.put("exito", "La orden ha sido registrada y asignada correctamente.");

      Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
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
      servicioOrdenReparacion.derivarCotizacion(idOrden, nivelReparacion);
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
}
