package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioHistorialOrden {
  void guardar(HistorialOrden historial);
  List<HistorialOrden> buscarPorOrden(Long ordenId);
}
