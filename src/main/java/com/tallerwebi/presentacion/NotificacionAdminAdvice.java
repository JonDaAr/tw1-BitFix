package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioOrdenReparacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = ControladorAdmin.class)
public class NotificacionAdminAdvice {

  private final ServicioOrdenReparacion servicioOrdenReparacion;

  @Autowired
  public NotificacionAdminAdvice(ServicioOrdenReparacion servicioOrdenReparacion) {
    this.servicioOrdenReparacion = servicioOrdenReparacion;
  }

  @ModelAttribute("cantidadRetirosPendientes")
  public int cantidadRetirosPendientes() {
    return servicioOrdenReparacion.obtenerOrdenesPendientesDeRetiro().size();
  }
}
