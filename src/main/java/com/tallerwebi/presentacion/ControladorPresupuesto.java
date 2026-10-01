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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPresupuesto {

  private static final String CODIGO_SEGUIMIENTO = "codigoSeguimiento";
  private static final String REPUESTOS = "repuestos";
  private static final String FORM = "form";
  private static final String PRESUPUESTO = "presupuesto";
  private static final String ERROR = "error";
  private static final String TOTAL = "total";
  private static final String SUBTOTAL = "subtotal";

  private final ServicioPresupuesto servicioPresupuesto;

  @Autowired
  public ControladorPresupuesto(ServicioPresupuesto servicioPresupuesto) {
    this.servicioPresupuesto = servicioPresupuesto;
  }

  @PostMapping("/presupuesto/calcular")
  public ModelAndView calcularPresupuestoMultiple(
    @ModelAttribute(FORM) PresupuestoMultipleForm form,
    @RequestParam(name = CODIGO_SEGUIMIENTO, required = false) Integer codigoSeguimiento
  ) {
    Map<String, Object> model = new HashMap<>();

    try {
      Double total = servicioPresupuesto.calcularTotalPresupuesto(form.getItems());

      model.put(TOTAL, total);
    } catch (SinStockException e) {
      model.put(ERROR, e.getMessage());
    }

    model.put(REPUESTOS, servicioPresupuesto.obtenerRepuestosDisponibles());
    model.put(FORM, form);
    model.put(CODIGO_SEGUIMIENTO, codigoSeguimiento);

    return new ModelAndView(PRESUPUESTO, model);
  }

  @PostMapping("/presupuesto/calcular-individual")
  public ModelAndView calcularSubtotal(Long repuestoId, Integer cantidad) {
    Map<String, Object> model = new HashMap<>();

    try {
      Double subtotal = servicioPresupuesto.calcularSubtotalRepuesto(repuestoId, cantidad);

      model.put(SUBTOTAL, subtotal);
    } catch (SinStockException e) {
      model.put(ERROR, e.getMessage());
    }

    model.put(REPUESTOS, servicioPresupuesto.obtenerRepuestosDisponibles());

    return new ModelAndView(PRESUPUESTO, model);
  }

  @PostMapping("/presupuesto/enviar")
  public ModelAndView enviarPresupuesto(
    @ModelAttribute(FORM) PresupuestoMultipleForm form,
    @RequestParam(CODIGO_SEGUIMIENTO) Integer codigoSeguimiento
  ) {
    Map<String, Object> model = new HashMap<>();

    try {
      Double total = servicioPresupuesto.calcularTotalPresupuesto(form.getItems());

      servicioPresupuesto.enviarPresupuesto(codigoSeguimiento, form.getItems(), total);

      model.put(TOTAL, total);
      model.put(CODIGO_SEGUIMIENTO, codigoSeguimiento);

      return new ModelAndView("presupuesto-enviado", model);
    } catch (SinStockException | IllegalArgumentException e) {
      model.put(ERROR, e.getMessage());

      model.put(REPUESTOS, servicioPresupuesto.obtenerRepuestosDisponibles());

      model.put(FORM, form);
      model.put(CODIGO_SEGUIMIENTO, codigoSeguimiento);

      return new ModelAndView(PRESUPUESTO, model);
    }
  }

  @GetMapping("/presupuesto")
  public ModelAndView irAPresupuesto(
    @RequestParam(name = "codigo", required = false) Integer codigoSeguimiento
  ) {
    Map<String, Object> model = new HashMap<>();

    List<Repuesto> repuestos = servicioPresupuesto.obtenerRepuestosDisponibles();

    PresupuestoMultipleForm form = new PresupuestoMultipleForm();

    if (repuestos != null) {
      for (Repuesto r : repuestos) {
        form.getItems().add(new ItemPresupuestoForm(r.getId(), 0));
      }
    }

    model.put(REPUESTOS, repuestos);
    model.put(FORM, form);
    model.put(CODIGO_SEGUIMIENTO, codigoSeguimiento);

    return new ModelAndView(PRESUPUESTO, model);
  }
}
