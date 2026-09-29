package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.EstadoOrden;
import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.OrdenNoEncontrado;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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

  //-----------------------

  @RequestMapping(path = "/ordenes")
  public ModelAndView irALaListaDeOrdenes() {
    Map<String, Object> modelo = new ModelMap();
    List<OrdenReparacion> ordenes = servicioOrdenReparacion.listarTodas();
    modelo.put("ordenes", ordenes);
    return new ModelAndView("lista-ordenes", modelo);
  }

  @RequestMapping(path = "/ordenes/editar")
  public ModelAndView irAEditarOrden(@RequestParam("idOrden") Long idOrden) {
    Map<String, Object> modelo = new ModelMap();
    OrdenReparacion ordenSeleccionada = servicioOrdenReparacion.buscarPorId(idOrden);

    //paso datos al DTO para q se muestre datos almacenados anteriormente en el form
    ActualizacionOrden ordenAActualizar = new ActualizacionOrden(
      ordenSeleccionada.getIdOrdenReparacion(),
      ordenSeleccionada.getModeloEquipo(),
      ordenSeleccionada.getEstado(),
      ordenSeleccionada.getNotaTecnica()
    );

    //para la lista de opciones
    modelo.put("ordenAActualizar", ordenAActualizar);

    modelo.put("estados", EstadoOrden.values());
    return new ModelAndView("editar-orden", modelo);
  }

  @RequestMapping(path = "/ordenes/actualizar", method = RequestMethod.POST)
  public ModelAndView actualizarOrden(
    @ModelAttribute("ordenAActualizar") ActualizacionOrden actualizacionOrden
  ) {
    try {
      servicioOrdenReparacion.actualizarEstadoYNotaTecnica(
        actualizacionOrden.getIdOrdenReparacion(),
        actualizacionOrden.getEstado(),
        actualizacionOrden.getNotaTecnica()
      );
    } catch (OrdenNoEncontrado e) {
      Map<String, Object> modelo = new ModelMap();
      modelo.put("error", "Orden no encontrado");
      modelo.put("ordenAActualizar", actualizacionOrden);
      modelo.put("estados", EstadoOrden.values());
      return new ModelAndView("editar-orden", modelo);
    }

    return new ModelAndView("redirect:/ordenes");
  }
}
