package com.tallerwebi.dominio;

import java.time.LocalDateTime;

public interface RepositorioOrdenReparacion {
  void guardarOrden(OrdenReparacion nuevaOrdenReparacion);
  OrdenReparacion buscarPorCodigo(Integer codigoSeguimiento);
  OrdenReparacion buscarPorId(Long id);
  void modificarOrden(OrdenReparacion orden);
  long contarOrdenesActivas(Long tecnicoId);
  LocalDateTime buscarFechaUltimaAsignacion(Long tecnicoId);
}
