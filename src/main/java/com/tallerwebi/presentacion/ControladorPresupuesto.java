package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.dominio.excepcion.SinStockException;
import jakarta.servlet.http.HttpSession;
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
  private final ServicioOrdenReparacion servicioOrdenReparacion;

  @Autowired
  public ControladorPresupuesto(
    ServicioPresupuesto servicioPresupuesto,
    ServicioOrdenReparacion servicioOrdenReparacion
  ) {
    this.servicioPresupuesto = servicioPresupuesto;
    this.servicioOrdenReparacion = servicioOrdenReparacion;
  }

  @PostMapping("/presupuesto/aceptar")
  public ModelAndView aceptarPresupuesto(
    @RequestParam("codigoSeguimiento") Integer codigoSeguimiento,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute("USUARIO");

    try {
      servicioOrdenReparacion.aceptarPresupuesto(codigoSeguimiento, usuario.getEmail());

      // NUEVO: Lo redirigimos al home con un mensaje de éxito (podés atrapar el mensaje en tu home.html si querés)
      return new ModelAndView("redirect:/home?exito=presupuesto_aceptado");
    } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
      Map<String, Object> model = new HashMap<>();
      model.put(ERROR, e.getMessage());
      return new ModelAndView("consultar-estado", model);
    }
  }

  @PostMapping("/presupuesto/rechazar")
  public ModelAndView rechazarPresupuesto(
    @RequestParam("codigoSeguimiento") Integer codigoSeguimiento,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute("USUARIO");

    try {
      servicioOrdenReparacion.rechazarPresupuesto(codigoSeguimiento, usuario.getEmail());

      return new ModelAndView("redirect:/home?exito=presupuesto_rechazado");
    } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
      Map<String, Object> model = new HashMap<>();
      model.put("error", e.getMessage());
      return new ModelAndView("consultar-estado", model);
    }
  }

  @PostMapping("/presupuesto/calcular")
  public ModelAndView calcularPresupuestoMultiple(
    @ModelAttribute(FORM) PresupuestoMultipleForm form,
    @RequestParam(name = CODIGO_SEGUIMIENTO, required = false) Integer codigoSeguimiento
  ) {
    Map<String, Object> model = new HashMap<>();

    try {
      Double subtotalRepuestos = servicioPresupuesto.calcularSubtotalRepuestos(form.getItems());
      Double manoDeObra = servicioPresupuesto.calcularManoDeObra(subtotalRepuestos);
      Double total = subtotalRepuestos + manoDeObra;

      model.put("subtotalRepuestos", subtotalRepuestos);
      model.put("manoDeObra", manoDeObra);
      model.put(TOTAL, total);
    } catch (SinStockException e) {
      model.put(ERROR, e.getMessage());
    }

    model.put(REPUESTOS, servicioPresupuesto.obtenerRepuestosDisponibles());
    model.put(FORM, form);
    model.put(CODIGO_SEGUIMIENTO, codigoSeguimiento);

    return new ModelAndView(PRESUPUESTO, model);
  }

  @PostMapping("/presupuesto/enviar")
  public ModelAndView enviarPresupuesto(
    @ModelAttribute(FORM) PresupuestoMultipleForm form,
    @RequestParam(CODIGO_SEGUIMIENTO) Integer codigoSeguimiento
  ) {
    Map<String, Object> model = new HashMap<>();

    try {
      Double subtotalRepuestos = servicioPresupuesto.calcularSubtotalRepuestos(form.getItems());
      Double manoDeObra = servicioPresupuesto.calcularManoDeObra(subtotalRepuestos);
      Double total = subtotalRepuestos + manoDeObra;

      servicioPresupuesto.enviarPresupuesto(codigoSeguimiento, form.getItems(), total);

      model.put("subtotalRepuestos", subtotalRepuestos);
      model.put("manoDeObra", manoDeObra);
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
