package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioCanje {
  void guardarComputadora(ComputadoraUsada computadora);
  ComputadoraUsada buscarComputadora(Long id);
  ComputadoraUsada buscarComputadoraConBloqueo(Long id);
  List<ComputadoraUsada> listarComputadorasDisponibles();
  List<ComputadoraUsada> listarComputadoras();
  void guardarSolicitud(SolicitudCanje solicitud);
  SolicitudCanje buscarSolicitud(Long id);
  List<SolicitudCanje> listarSolicitudes();
  List<SolicitudCanje> listarSolicitudesDeCliente(Long clienteId);
}
