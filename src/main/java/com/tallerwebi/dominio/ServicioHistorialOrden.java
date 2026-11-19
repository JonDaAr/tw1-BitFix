package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioHistorialOrden {
  void registrarEvento(OrdenReparacion orden, EstadoOrden estado);
  List<HistorialOrden> obtenerHistorial(Long ordenId);
}
