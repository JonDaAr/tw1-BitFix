package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Repuesto;
import com.tallerwebi.dominio.ServicioPresupuesto;
import com.tallerwebi.dominio.excepcion.SinStockException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
    Map<String, Object> model = new HashMap<>();
    List<Repuesto> repuestos = servicioPresupuesto.obtenerRepuestosDisponibles();

    PresupuestoMultipleForm form = new PresupuestoMultipleForm();
    if (repuestos != null) {
      for (Repuesto r : repuestos) {
        form.getItems().add(new ItemPresupuestoForm(r.getId(), 0));
      }
    }

    model.put("repuestos", repuestos);
    model.put("form", form);
    return new ModelAndView("presupuesto", model);
  }

  @PostMapping("/presupuesto/calcular")
  public ModelAndView calcularPresupuestoMultiple(
    @ModelAttribute("form") PresupuestoMultipleForm form
  ) {
    Map<String, Object> model = new HashMap<>();
    try {
      Double total = servicioPresupuesto.calcularTotalPresupuesto(form.getItems());
      model.put("total", total);
    } catch (SinStockException e) {
      model.put("error", e.getMessage());
    }

    // Volvemos a pasar la lista para que la tabla no se vacíe
    model.put("repuestos", servicioPresupuesto.obtenerRepuestosDisponibles());
    model.put("form", form);
    return new ModelAndView("presupuesto", model);
  }

  @PostMapping("/presupuesto/calcular-individual")
  public ModelAndView calcularSubtotal(Long repuestoId, Integer cantidad) {
    Map<String, Object> model = new HashMap<>();
    try {
      Double subtotal = servicioPresupuesto.calcularSubtotalRepuesto(repuestoId, cantidad);
      model.put("subtotal", subtotal);
    } catch (SinStockException e) {
      model.put("error", e.getMessage());
    }
    model.put("repuestos", servicioPresupuesto.obtenerRepuestosDisponibles());
    return new ModelAndView("presupuesto", model);
  }
}
