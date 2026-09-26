package com.tallerwebi.dominio;

import java.time.LocalDateTime;

public interface RepositorioOrden {
  void guardar(Orden orden);

  long contarOrdenesActivas(Long tecnicoId);

  LocalDateTime buscarFechaUltimaAsignacion(Long tecnicoId);
}
