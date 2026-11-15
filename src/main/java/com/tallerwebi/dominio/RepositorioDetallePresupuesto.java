package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioDetallePresupuesto {
  void guardar(DetallePresupuesto detalle);

  void modificar(DetallePresupuesto detalle);

  List<DetallePresupuesto> buscarPorOrden(Long ordenId);
}
