package com.tallerwebi.dominio;

public class CalculadorDistancia {

  private static final int RADIO_TIERRA_KM = 6371;

  /**
   * Calcula la distancia en kilómetros entre dos puntos geográficos usando la fórmula de Haversine.
   */
  public static double calcularDistancia(
    Double latOrigen,
    Double lonOrigen,
    Double latDestino,
    Double lonDestino
  ) {
    if (latOrigen == null || lonOrigen == null || latDestino == null || lonDestino == null) {
      throw new IllegalArgumentException("Las coordenadas no pueden ser nulas");
    }

    double dLat = Math.toRadians(latDestino - latOrigen);
    double dLon = Math.toRadians(lonDestino - lonOrigen);

    double terminoA =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(Math.toRadians(latOrigen)) *
        Math.cos(Math.toRadians(latDestino)) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);

    double distanciaAngular = 2 * Math.atan2(Math.sqrt(terminoA), Math.sqrt(1 - terminoA));

    // Retorna la distancia en Kilómetros
    return RADIO_TIERRA_KM * distanciaAngular;
  }
}
