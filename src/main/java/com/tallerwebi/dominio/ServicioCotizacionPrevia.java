package com.tallerwebi.dominio;

public interface ServicioCotizacionPrevia {
  void enviarCotizacionPrevia(Long idOrden, Double montoEstimado, String notaTecnica);

  void aceptarCotizacionPrevia(Integer codigoSeguimiento, String emailUsuario);

  void rechazarCotizacionPrevia(Integer codigoSeguimiento, String emailUsuario);
}
