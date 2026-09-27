package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Repuesto;
import com.tallerwebi.dominio.ServicioPresupuesto;
import com.tallerwebi.dominio.excepcion.SinStockException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPresupuesto {

  private final ServicioPresupuesto servicioPresupuesto;

  @Autowired
  public ControladorPresupuesto(ServicioPresupuesto servicioPresupuesto) {
    this.servicioPresupuesto = servicioPresupuesto;
  }

  @GetMapping("/presupuesto")
  public ModelAndView irAPresupuesto() {
    ModelAndView modelAndView = new ModelAndView("presupuesto");
    List<Repuesto> repuestos = this.servicioPresupuesto.obtenerRepuestosDisponibles();
    modelAndView.addObject("repuestos", repuestos);
    return modelAndView;
  }

  @PostMapping("/presupuesto/calcular")
  public ModelAndView calcularSubtotal(
    @RequestParam("repuestoId") Long repuestoId,
    @RequestParam("cantidad") Integer cantidad
  ) {
    ModelAndView modelAndView = new ModelAndView("presupuesto");
    try {
      Double subtotal = this.servicioPresupuesto.calcularSubtotalRepuesto(repuestoId, cantidad);
      modelAndView.addObject("subtotal", subtotal);
    } catch (SinStockException e) {
      modelAndView.addObject("error", e.getMessage());
    }

    modelAndView.addObject("repuestos", this.servicioPresupuesto.obtenerRepuestosDisponibles());
    return modelAndView;
  }
}
