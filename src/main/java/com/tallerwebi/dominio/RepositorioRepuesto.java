package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioRepuesto {
  void guardar(Repuesto repuesto);
  Repuesto buscarPorId(Long id);
  List<Repuesto> obtenerTodos();
  List<Repuesto> obtenerDisponibles();
  List<Repuesto> obtenerRepuestosDisponibles();
}
