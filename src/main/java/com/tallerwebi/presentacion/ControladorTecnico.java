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

    Map<String, Object> model = new HashMap<>();

    List<OrdenReparacion> ordenesAsignadas = servicioOrdenReparacion.obtenerOrdenesDelTecnico(
      usuario.getId()
    );

    model.put("ordenesAsignadas", ordenesAsignadas);
    model.put("vista", "ordenes");
    return new ModelAndView("panel-tecnico", model);
  }

  @GetMapping("/nueva-orden")
  public ModelAndView nuevaOrden() {
    Map<String, Object> model = new HashMap<>();

    model.put("datosOrden", new DatosOrden());

    model.put("vista", "nueva-orden");

    return new ModelAndView("panel-tecnico", model);
  }

  @GetMapping("/historial")
  public ModelAndView verHistorialOrdenesCompletadas(HttpSession session) {
    Map<String, Object> model = new HashMap<>();

    Usuario usuario = (Usuario) session.getAttribute("USUARIO");

    List<OrdenReparacion> historial = servicioOrdenReparacion.obtenerHistorialDelTecnico(
      usuario.getId()
    );

    model.put("historial", historial);
    model.put("vista", "historial-tecnico");
    model.put("tecnico", usuario);

    return new ModelAndView("panel-tecnico", model);
  }
}
