package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.SinStockException;
import java.util.List;

public interface ServicioPresupuesto {
  List<Repuesto> obtenerRepuestosDisponibles();
  Double calcularSubtotalRepuesto(Long idRepuesto, Integer cantidad) throws SinStockException;
  void descontarStockRepuesto(Long idRepuesto, Integer cantidad) throws SinStockException;
}
