package com.tallerwebi.dominio;

import java.time.LocalDateTime;
import java.util.List;

public interface RepositorioOrdenReparacion {
  void guardarOrden(OrdenReparacion nuevaOrdenReparacion);
  OrdenReparacion buscarPorCodigo(Integer codigoSeguimiento);
  OrdenReparacion buscarPorId(Long id);
  void modificarOrden(OrdenReparacion orden);
  long contarOrdenesActivas(Long tecnicoId);
  LocalDateTime buscarFechaUltimaAsignacion(Long tecnicoId);
  List<OrdenReparacion> buscarTodas();

  List<OrdenReparacion> listarTodasLasOrdenes();
}
