package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioSucursal;
import com.tallerwebi.dominio.Sucursal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorSucursal {

  private final ServicioSucursal servicioSucursal;

  @Autowired
  public ControladorSucursal(ServicioSucursal servicioSucursal) {
    this.servicioSucursal = servicioSucursal;
  }

  // 1. Solo devuelve la vista HTML limpia, sin parámetros ni lógica de negocio
  @RequestMapping("/sucursales")
  public ModelAndView listarSucursales() {
    return new ModelAndView("sucursales");
  }

  // 2. Toda la lógica de negocio y cálculo vive acá, respondiendo en JSON
  @RequestMapping("/api/sucursales")
  @ResponseBody
  public List<Sucursal> listarSucursalesJson(
    @RequestParam(value = "lat", required = false) Double lat,
    @RequestParam(value = "lon", required = false) Double lon
  ) {
    return servicioSucursal.obtenerSucursalesCercanas(lat, lon);
  }
}
