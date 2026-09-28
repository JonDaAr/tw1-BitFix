package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
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

  @GetMapping("/consultar-estado")
  public ModelAndView irAConsulta() {
    Map<String, Object> model = new HashMap<>();
    model.put("datosConsulta", new DatosConsultaEstado());
    return new ModelAndView("consultar-estado", model);
  }

  @PostMapping("/consulta-estado")
  public ModelAndView buscarEstado(@ModelAttribute("datosConsulta") DatosConsultaEstado datos) {
    Map<String, Object> model = new HashMap<>();
    try {
      Integer codigoInt = Integer.valueOf(datos.getCodigoSeguimiento());
      OrdenReparacion pedido = servicioOrdenReparacion.consultarEstado(codigoInt);
      model.put("pedido", pedido);
      return new ModelAndView("resultado-consulta", model);
    } catch (NumberFormatException e) {
      model.put("error", "El código de seguimiento debe ser un número válido");
      return new ModelAndView("consultar-estado", model);
    } catch (PedidoNoEncontradoException e) {
      model.put("error", e.getMessage());
      return new ModelAndView("consultar-estado", model);
    }
  }
}
