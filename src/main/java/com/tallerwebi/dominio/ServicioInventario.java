package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioInventario {
  List<Repuesto> listarTodos();
  Repuesto buscarPorId(Long id);
  void guardarOActualizar(Repuesto repuesto);
  void eliminar(Long id);
}
