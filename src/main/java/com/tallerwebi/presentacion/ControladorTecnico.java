package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.ServicioPresupuesto;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    Usuario usuario = (Usuario) session.getAttribute("USUARIO");

    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    Map<String, Object> model = new HashMap<>();

    List<OrdenReparacion> ordenesAsignadas = servicioOrdenReparacion.obtenerOrdenesDelTecnico(
      usuario.getId()
    );

    model.put("ordenesAsignadas", ordenesAsignadas);
    return new ModelAndView("panel-tecnico", model);
  }

  @PostMapping("/orden/enviar-presupuesto")
  public ModelAndView enviarPresupuestoCliente(
    @RequestParam("codigoSeguimiento") Integer codigoSeguimiento,
    @RequestParam("costoManoDeObra") Double costoManoDeObra,
    @RequestParam("diasEstimados") Integer diasEstimados,
    @RequestParam("diagnostico") String diagnostico,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute("USUARIO");

    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    Map<String, Object> model = new HashMap<>();

    try {
      servicioPresupuesto.generarYEnviarPresupuesto(
        codigoSeguimiento,
        costoManoDeObra,
        diagnostico
      );

      model.put(
        "mensaje",
        "El presupuesto para la orden #" + codigoSeguimiento + " se envió correctamente al cliente."
      );
    } catch (Exception e) {
      model.put("error", "Error al procesar el presupuesto: " + e.getMessage());
    }

    model.put(
      "ordenesAsignadas",
      servicioOrdenReparacion.obtenerOrdenesDelTecnico(usuario.getId())
    );

    model.put("ordenesAsignadas", servicioOrdenReparacion.obtenerOrdenesParaTecnico());
    model.put("datosOrden", new DatosOrden());
    return new ModelAndView("panel-tecnico", model);
  }

  @GetMapping("/nueva-orden")
  public ModelAndView nuevaOrden() {
    Map<String, Object> model = new HashMap<>();

    model.put("datosOrden", new DatosOrden());

    model.put("vista", "nueva-orden");

    return new ModelAndView("panel-tecnico", model);
  }
}
