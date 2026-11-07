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
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorSucursal {

  private final ServicioSucursal servicioSucursal;

  @Autowired
  public ControladorSucursal(ServicioSucursal servicioSucursal) {
    this.servicioSucursal = servicioSucursal;
  }

  @RequestMapping("/sucursales")
  public ModelAndView listarSucursales(
    @RequestParam(value = "lat", required = false) Double lat,
    @RequestParam(value = "lon", required = false) Double lon
  ) {
    Map modelo = new HashMap();
    List<Sucursal> sucursales = servicioSucursal.obtenerSucursalesCercanas(lat, lon);

    modelo.put("sucursales", sucursales);
    modelo.put("latReferencia", lat);
    modelo.put("lonReferencia", lon);

    return new ModelAndView("sucursales", modelo);
  }
}
