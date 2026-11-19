package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioSucursalImpl implements ServicioSucursal {

  private final RepositorioSucursal repositorioSucursal;

  @Autowired
  public ServicioSucursalImpl(RepositorioSucursal repositorioSucursal) {
    this.repositorioSucursal = repositorioSucursal;
  }

  @Override
  public List<Sucursal> obtenerSucursalesCercanas(Double lat, Double lon) {
    // 1. Traemos todas las sucursales de forma estándar
    List<Sucursal> sucursales = repositorioSucursal.obtenerTodas();

    // Si el usuario no mandó ubicación, devolvemos la lista tal cual
    if (lat == null || lon == null) {
      return sucursales;
    }

    // 2. Calculamos la distancia para cada sucursal en memoria
    for (Sucursal s : sucursales) {
      double distanciaKm = calcularDistanciaHaversine(lat, lon, s.getLatitud(), s.getLongitud());
      s.setDistancia(distanciaKm); // Se llena el campo @Transient
    }

    // 3. Ordenamos la lista de menor a mayor distancia usando Java Streams/Comparator
    sucursales.sort(java.util.Comparator.comparing(Sucursal::getDistancia));

    return sucursales;
  }

  // Método auxiliar privado con la fórmula matemática de Haversine
  private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
    final int RADIO_TIERRA_KM = 6371;

    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double componenteA =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(Math.toRadians(lat1)) *
        Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);

    double anguloC = 2 * Math.atan2(Math.sqrt(componenteA), Math.sqrt(1 - componenteA));

    return RADIO_TIERRA_KM * anguloC;
  }
}
