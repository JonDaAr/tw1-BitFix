package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Repuesto;
import com.tallerwebi.dominio.ServicioInventario;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorInventario {

  private final ServicioInventario servicioInventario;

  @Autowired
  public ControladorInventario(ServicioInventario servicioInventario) {
    this.servicioInventario = servicioInventario;
  }

  @GetMapping("/inventario")
  public ModelAndView verInventario() {
    Map<String, Object> model = new HashMap<>();
    model.put("repuestos", servicioInventario.listarTodos());
    model.put("nuevoRepuesto", new Repuesto());
    return new ModelAndView("inventario", model);
  }

  @PostMapping("/inventario/guardar")
  public ModelAndView guardarRepuesto(@ModelAttribute("nuevoRepuesto") Repuesto repuesto) {
    servicioInventario.guardarOActualizar(repuesto);
    return new ModelAndView("redirect:/inventario");
  }

  @GetMapping("/inventario/editar/{id}")
  public ModelAndView editarRepuesto(@PathVariable("id") Long id) {
    Map<String, Object> model = new HashMap<>();
    model.put("repuestos", servicioInventario.listarTodos());
    model.put("nuevoRepuesto", servicioInventario.buscarPorId(id));
    return new ModelAndView("inventario", model);
  }

  @GetMapping("/inventario/eliminar/{id}")
  public ModelAndView eliminarRepuesto(@PathVariable("id") Long id) {
    servicioInventario.eliminar(id);
    return new ModelAndView("redirect:/inventario");
  }
}
