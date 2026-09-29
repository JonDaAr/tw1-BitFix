package com.tallerwebi.dominio;

public interface RepositorioOrdenReparacion {
  void guardarOrden(OrdenReparacion nuevaOrdenReparacion);
  OrdenReparacion buscarPorCodigo(Integer codigoSeguimiento);
  OrdenReparacion buscarPorId(Long id);
  void modificarOrden(OrdenReparacion orden);
}
