package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ResultadoCotizacionLogistica;
import com.tallerwebi.dominio.ServicioLogistica;
import com.tallerwebi.dominio.Sucursal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControladorLogistica {

  private final ServicioLogistica servicioLogistica;

  @Autowired
  public ControladorLogistica(ServicioLogistica servicioLogistica) {
    this.servicioLogistica = servicioLogistica;
  }

  @RequestMapping(path = "/api/logistica/cotizar", method = RequestMethod.GET)
  @ResponseBody
  public ResultadoCotizacionLogistica cotizarEnvio(
    @RequestParam("lat") Double latitud,
    @RequestParam("lon") Double longitud
  ) {
    return servicioLogistica.cotizarRetiro(latitud, longitud);
  }

  @RequestMapping(path = "/api/logistica/sucursales", method = RequestMethod.GET)
  @ResponseBody
  public List<Sucursal> listarSucursales() {
    // Al usar @ResponseBody y tener Jackson configurado, esto devuelve un Array JSON [ {id: 1, latitud: -34...}, ... ]
    return servicioLogistica.obtenerSucursalesOperativas();
  }
}
