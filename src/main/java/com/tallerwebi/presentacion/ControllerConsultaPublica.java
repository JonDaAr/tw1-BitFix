package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Pedido;
import com.tallerwebi.dominio.ServicioPedido;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControllerConsultaPublica {

  private final ServicioPedido servicioPedido;

  @Autowired
  public ControllerConsultaPublica(ServicioPedido servicioPedido) {
    this.servicioPedido = servicioPedido;
  }

  @GetMapping("/consultar-estado")
  @SuppressWarnings("PMD.LooseCoupling")
  public ModelAndView irAConsulta() {
    ModelMap model = new ModelMap();
    model.put("datosConsulta", new DatosConsultaEstado());
    return new ModelAndView("consultar-estado", model);
  }

  @PostMapping("/consulta-estado")
  public ModelAndView buscarEstado(@ModelAttribute("datosConsulta") DatosConsultaEstado datos) {
    ModelMap model = new ModelMap();
    try {
      Pedido pedido = servicioPedido.consultarEstado(datos.getCodigoSeguimiento());
      model.put("pedido", pedido);
      return new ModelAndView("resultado-consulta", model);
    } catch (PedidoNoEncontradoException e) {
      model.put("error", e.getMessage());
      return new ModelAndView("consulta-estado", model);
    }
  }
}
