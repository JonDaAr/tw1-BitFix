package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRecepcion {

  private final ServicioOrdenReparacion servicioOrden;

  @Autowired
  public ControladorRecepcion(ServicioOrdenReparacion servicioOrden) {
    this.servicioOrden = servicioOrden;
  }

  @RequestMapping(path = "/recepcion", method = RequestMethod.GET)
  public ModelAndView mostrarRecepcion() {
    Map<String, Object> model = new HashMap<>();

    model.put("datosOrden", new DatosOrden());

    return new ModelAndView("recepcion", model);
  }

  @RequestMapping(path = "/recepcion", method = RequestMethod.POST)
  public ModelAndView registrarOrden(@ModelAttribute("datosOrden") DatosOrden datosOrden) {
    OrdenReparacion orden = crearOrden(datosOrden);

    try {
      OrdenReparacion ordenRegistrada = servicioOrden.registrarOrden(orden);

      Map<String, Object> model = new HashMap<>();

      model.put("orden", ordenRegistrada);

      return new ModelAndView("confirmacion-orden", model);
    } catch (NoHayTecnicosDisponibles e) {
      Map<String, Object> model = new HashMap<>();

      model.put("error", "No hay tecnicos disponibles para asignar la orden");

      model.put("datosOrden", datosOrden);

      return new ModelAndView("recepcion", model);
    }
  }

  private OrdenReparacion crearOrden(DatosOrden datosOrden) {
    OrdenReparacion orden = new OrdenReparacion();

    orden.setNombreCliente(datosOrden.getNombreCliente());

    orden.setEmailCliente(datosOrden.getEmailCliente());

    orden.setModeloEquipo(datosOrden.getModeloEquipo());

    orden.setDescripcionFalla(datosOrden.getDescripcionFalla());

    orden.setAccesorios(datosOrden.getAccesorios());

    return orden;
  }
}
