package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Orden;
import com.tallerwebi.dominio.ServicioOrden;
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

  private final ServicioOrden servicioOrden;

  @Autowired
  public ControladorRecepcion(ServicioOrden servicioOrden) {
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
    Orden orden = crearOrden(datosOrden);

    try {
      Orden ordenRegistrada = servicioOrden.registrarOrden(orden);

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

  private Orden crearOrden(DatosOrden datosOrden) {
    Orden orden = new Orden();

    orden.setNombreCliente(datosOrden.getNombreCliente());

    orden.setEmailCliente(datosOrden.getEmailCliente());

    orden.setTipoEquipo(datosOrden.getTipoEquipo());

    orden.setFallaReportada(datosOrden.getFallaReportada());

    orden.setAccesorios(datosOrden.getAccesorios());

    return orden;
  }
}
