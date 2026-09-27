package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorOrdenReparacion {

  private ServicioOrdenReparacion servicioOrdenReparacion;

  @Autowired
  public ControladorOrdenReparacion(ServicioOrdenReparacion servicioOrdenReparacion) {
    this.servicioOrdenReparacion = servicioOrdenReparacion;
  }

  @RequestMapping(path = "/registrar-orden", method = RequestMethod.GET)
  public ModelAndView irAFormularioRegistrarOrdenReparacion() {
    Map<String, Object> modelo = new ModelMap();

    modelo.put("datosOrden", new DatosOrden());

    return new ModelAndView("registro-orden-reparacion", modelo);
  }

  @RequestMapping(path = "/registrar-orden", method = RequestMethod.POST)
  public ModelAndView registrarOrdenReparacion(
    @ModelAttribute("datosOrden") DatosOrden datosOrden
  ) {
    Map<String, Object> modelo = new ModelMap();
    try {
      OrdenReparacion nuevaOrdenReparacion =
        this.servicioOrdenReparacion.registrarOrden(datosOrden);

      modelo.put("codigoSeguimiento", nuevaOrdenReparacion.getCodigoSeguimiento());

      return new ModelAndView("confirmacion-nueva-orden-reparacion", modelo);
    } catch (DatosIncompletosException e) {
      modelo.put(
        "error",
        "No se puede registrar una orden con datos incompletos. Por favor, complete todos los campos."
      );
//que no tenga menos de 10, que no sean letras (telefono)
      //que los demas campos tengan minimo 3 caracteres
      return new ModelAndView("registro-orden-reparacion", modelo);
    }
  }
}
