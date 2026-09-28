package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioOrdenReparacion {
  void guardarOrden(OrdenReparacion nuevaOrdenReparacion);

  OrdenReparacion buscarOrdenPorCodigo(Integer codigoSeguimiento);

  //
  OrdenReparacion buscarPorIdOrdenReparacion(Long idOrdenReparacion);
  List<OrdenReparacion> listarTodasLasOrdenes();

}
