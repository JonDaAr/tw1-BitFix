package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.presentacion.DatosOrden;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ServicioOrdenReparacion {
  OrdenReparacion registrarOrden(DatosOrden orden) throws DatosIncompletosException;
}
