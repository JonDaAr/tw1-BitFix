package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.SinStockException;
import com.tallerwebi.presentacion.ItemPresupuestoForm;
import java.util.List;

public interface ServicioPresupuesto {
  List<Repuesto> obtenerRepuestosDisponibles();

  Double calcularSubtotal(Long repuestoId, Integer cantidad) throws SinStockException;

  Double calcularSubtotalRepuesto(Long repuestoId, Integer cantidad) throws SinStockException;

  void descontarStockRepuesto(Long repuestoId, Integer cantidad) throws SinStockException;

  Double calcularTotalPresupuesto(List<ItemPresupuestoForm> items) throws SinStockException;

  void generarYEnviarPresupuesto(
    Integer codigoSeguimiento,
    Double costoManoDeObra,
    String diagnostico
  );

  void enviarPresupuesto(Integer codigoSeguimiento, List<ItemPresupuestoForm> items, Double total);
}
