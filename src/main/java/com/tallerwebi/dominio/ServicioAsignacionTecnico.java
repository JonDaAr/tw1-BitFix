package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;

@FunctionalInterface
public interface ServicioAsignacionTecnico {
  void asignarTecnicoAOrden(OrdenReparacion orden) throws NoHayTecnicosDisponibles;
}
