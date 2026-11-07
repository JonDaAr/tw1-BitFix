package com.tallerwebi.dominio;

import java.util.List;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ServicioSucursal {
  List<Sucursal> obtenerSucursalesCercanas(Double lat, Double lon);
}
