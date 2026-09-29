package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioCierreOrden;
import com.tallerwebi.dominio.excepcion.EstadoInvalidoParaCierreException;
import com.tallerwebi.dominio.excepcion.OrdenNoEncontradaException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorCierreOrden {

  private static final String ATRIBUTO_ERROR = "error";
  private static final String VISTA_ERROR = "error";

  private ServicioCierreOrden servicioCierreOrden;

  @Autowired
  public ControladorCierreOrden(ServicioCierreOrden servicioCierreOrden) {
    this.servicioCierreOrden = servicioCierreOrden;
  }

  @GetMapping("/cierre-orden/{idOrden}")
  public ModelAndView verPantallaCierre(@PathVariable("idOrden") Long idOrden) {
    Map<String, Object> modelo = new ModelMap();
    try {
      OrdenReparacion orden = servicioCierreOrden.obtenerOrdenParaComprobante(idOrden);
      modelo.put("orden", orden);
      return new ModelAndView("cierre-orden", modelo);
    } catch (OrdenNoEncontradaException e) {
      modelo.put(ATRIBUTO_ERROR, e.getMessage());
      return new ModelAndView(VISTA_ERROR, modelo);
    }
  }

  @PostMapping("/cerrar-orden")
  public ModelAndView cerrarOrden(@RequestParam("idOrden") Long idOrden) {
    Map<String, Object> modelo = new ModelMap();
    try {
      OrdenReparacion ordenCerrada = servicioCierreOrden.cerrarOrden(idOrden);
      return new ModelAndView("redirect:/comprobante/" + ordenCerrada.getIdOrdenReparacion());
    } catch (OrdenNoEncontradaException | EstadoInvalidoParaCierreException e) {
      modelo.put(ATRIBUTO_ERROR, e.getMessage());
      return new ModelAndView(VISTA_ERROR, modelo);
    }
  }

  @GetMapping("/comprobante/{idOrden}")
  public ModelAndView verComprobante(@PathVariable("idOrden") Long idOrden) {
    Map<String, Object> modelo = new ModelMap();
    try {
      OrdenReparacion orden = servicioCierreOrden.obtenerOrdenParaComprobante(idOrden);
      modelo.put("orden", orden);
      return new ModelAndView("comprobante-entrega", modelo);
    } catch (OrdenNoEncontradaException e) {
      modelo.put(ATRIBUTO_ERROR, e.getMessage());
      return new ModelAndView(VISTA_ERROR, modelo);
    }
  }

  @GetMapping("/crear-orden-prueba")
  public ModelAndView crearOrdenDePrueba() {
    OrdenReparacion ordenPrueba = servicioCierreOrden.crearOrdenPruebaReparada();
    return new ModelAndView("redirect:/cierre-orden/" + ordenPrueba.getIdOrdenReparacion());
  }
}
