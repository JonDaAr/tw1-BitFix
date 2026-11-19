package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CalculadorDistanciaTest {

  @Test
  public void deberiaLanzarExcepcionSiAlgunaCoordenadaEsNula() {
    assertThrows(
      IllegalArgumentException.class,
      () -> {
        CalculadorDistancia.calcularDistancia(null, -58.0, -34.0, -58.0);
      }
    );
  }

  @Test
  public void deberiaCalcularDistanciaCorrectamente() {
    // Coordenadas del centro de Morón
    Double latMoron = -34.6531;
    Double lonMoron = -58.6206;

    // Coordenadas de la UNLaM
    Double latUnlam = -34.6704;
    Double lonUnlam = -58.5614;

    // La distancia real en línea recta es aproximadamente 5.7 km
    double distancia = CalculadorDistancia.calcularDistancia(
      latMoron,
      lonMoron,
      latUnlam,
      lonUnlam
    );

    // Validamos que el cálculo de con un margen de error aceptable (0.5 km)
    assertThat(distancia, is(closeTo(5.7, 0.5)));
  }
}
