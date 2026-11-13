package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioAlerta {
  boolean existeAlertaPendienteConMensaje(String mensaje);
  void guardar(Alerta alerta);
  List<Alerta> obtenerAlertasPendientes();
  Alerta buscarPorId(Long id);
  void borrar(Alerta alerta);
  void eliminarPorUrlAccion(String urlAccion);
}
