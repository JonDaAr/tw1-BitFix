package com.tallerwebi.dominio;

public interface RepositorioOrdenReparacion {
  void guardarOrden(OrdenReparacion nuevaOrdenReparacion);

  OrdenReparacion buscarPorCodigo(Integer codigoSeguimiento);
}
