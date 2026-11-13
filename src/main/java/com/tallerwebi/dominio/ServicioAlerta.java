package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.ItemPresupuestoForm;
import java.util.List;

public interface ServicioAlerta {
  void crearAlertaConAccion(String mensaje, String urlAccion);
  List<Alerta> obtenerAlertasPendientes();
  void marcarComoLeida(Long id);
  void crearAlertasPorFaltaDeStock(List<ItemPresupuestoForm> listaAValidarStock);
  void limpiarAlertaSiEsNecesariaPorFaltaDeStock(Long idRepuesto);
  void limpiarAlertaPorRepuestoEliminado(Long idRepuesto);
}
