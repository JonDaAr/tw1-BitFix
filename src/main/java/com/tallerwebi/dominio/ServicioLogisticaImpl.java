package com.tallerwebi.dominio;

import com.tallerwebi.dominio.CalculadorDistancia;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioLogisticaImpl implements ServicioLogistica {

  private final RepositorioSucursal repositorioSucursal;

  // Reglas de negocio de tu logística
  private static final double RADIO_MAXIMO_KM = 15.0; // Tope máximo de viaje
  private static final double COSTO_BASE = 2500.0; // Bajada de bandera del chofer
  private static final double COSTO_POR_KM = 600.0; // Valor del kilómetro recorrido

  @Autowired
  public ServicioLogisticaImpl(RepositorioSucursal repositorioSucursal) {
    this.repositorioSucursal = repositorioSucursal;
  }

  @Override
  public ResultadoCotizacionLogistica cotizarRetiro(Double latCliente, Double lonCliente) {
    ResultadoCotizacionLogistica resultado = new ResultadoCotizacionLogistica();

    List<Sucursal> sucursales = repositorioSucursal.obtenerTodas();

    if (sucursales == null || sucursales.isEmpty()) {
      resultado.setViable(false);
      resultado.setMensajeError("No hay sucursales operativas en este momento.");
      return resultado;
    }

    Sucursal masCercana = null;
    double menorDistancia = Double.MAX_VALUE;

    for (Sucursal sucursal : sucursales) {
      if (sucursal.getLatitud() != null && sucursal.getLongitud() != null) {
        double distancia = CalculadorDistancia.calcularDistancia(
          latCliente,
          lonCliente,
          sucursal.getLatitud(),
          sucursal.getLongitud()
        );

        if (distancia < menorDistancia) {
          menorDistancia = distancia;
          masCercana = sucursal;
        }
      }
    }

    if (masCercana == null) {
      resultado.setViable(false);
      resultado.setMensajeError(
        "Nuestras sucursales no tienen coordenadas configuradas para calcular el envío."
      );
      return resultado;
    }

    if (menorDistancia > RADIO_MAXIMO_KM) {
      resultado.setViable(false);
      resultado.setMensajeError(
        "Tu domicilio se encuentra a " +
        String.format("%.1f", menorDistancia) +
        " km. Supera nuestra área de cobertura máxima (" +
        RADIO_MAXIMO_KM +
        " km)."
      );
      return resultado;
    }

    resultado.setViable(true);
    resultado.setSucursalAsignada(masCercana);

    // Redondeamos a 2 decimales
    resultado.setDistanciaKm(Math.round(menorDistancia * 100.0) / 100.0);

    // Calculamos el total y lo guardamos
    double costoCalculado = COSTO_BASE + (menorDistancia * COSTO_POR_KM);
    resultado.setCostoTotal(Math.round(costoCalculado * 100.0) / 100.0);

    return resultado;
  }

  @Override
  public List<Sucursal> obtenerSucursalesOperativas() {
    return repositorioSucursal.obtenerTodas();
  }
}
