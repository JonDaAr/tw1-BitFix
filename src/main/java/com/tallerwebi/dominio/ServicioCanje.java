package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioCanje {
  List<ComputadoraUsada> listarDisponibles();
  List<ComputadoraUsada> listarTodasLasComputadoras();
  List<SolicitudCanje> listarSolicitudes();
  List<SolicitudCanje> listarSolicitudesDeCliente(Long clienteId);
  void guardarComputadora(ComputadoraUsada computadora);
  void pausarComputadora(Long computadoraId);
  void solicitarCanje(
    Long computadoraId,
    Usuario cliente,
    String marca,
    String modelo,
    String descripcion,
    Double valorOfrecido
  );
  void resolverSolicitud(Long solicitudId, boolean aprobar, Double valorAprobado);
}
