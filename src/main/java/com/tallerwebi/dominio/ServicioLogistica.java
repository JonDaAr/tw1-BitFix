package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioLogistica {
  ResultadoCotizacionLogistica cotizarRetiro(Double latCliente, Double lonCliente);
  List<Sucursal> obtenerSucursalesOperativas();
}
