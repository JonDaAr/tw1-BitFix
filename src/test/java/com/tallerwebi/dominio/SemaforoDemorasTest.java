package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class SemaforoDemorasTest {

  @Test
  public void ordenDeMasDe8DiasTienePrioridadAlta() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setFechaIngreso(LocalDateTime.now().minusDays(10));

    Prioridad prioridad = orden.calcularPrioridad(LocalDateTime.now());

    assertThat(prioridad, is(Prioridad.ALTA));
  }

  @Test
  public void ordenDe4A7DiasTienePrioridadMedia() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setFechaIngreso(LocalDateTime.now().minusDays(7));

    Prioridad prioridad = orden.calcularPrioridad(LocalDateTime.now());

    assertThat(prioridad, is(Prioridad.MEDIA));
  }

  @Test
  public void ordenDeMenosDe4DiasTienePrioridadBaja() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setFechaIngreso(LocalDateTime.now().minusDays(3));

    Prioridad prioridad = orden.calcularPrioridad(LocalDateTime.now());

    assertThat(prioridad, is(Prioridad.BAJA));
  }
}
