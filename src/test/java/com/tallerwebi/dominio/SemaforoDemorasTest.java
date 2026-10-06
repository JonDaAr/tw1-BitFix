package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.excepcion.FechaIngresoNoDefinidaException;
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

  @Test
  public void ordenSinFechaIngresoLanzaExcepcion() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setFechaIngreso(null);

    assertThrows(
      FechaIngresoNoDefinidaException.class,
      () -> orden.calcularPrioridad(LocalDateTime.now())
    );
  }

  @Test
  public void ordenViejaEsRoja() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setFechaIngreso(LocalDateTime.now().minusDays(10));

    assertThat(orden.getColorPrioridad(), is("rojo"));
  }

  @Test
  public void ordenMediaEsAmarilla() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setFechaIngreso(LocalDateTime.now().minusDays(5));

    assertThat(orden.getColorPrioridad(), is("amarillo"));
  }

  @Test
  public void ordenRecienteEsVerde() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setFechaIngreso(LocalDateTime.now().minusDays(1));

    assertThat(orden.getColorPrioridad(), is("verde"));
  }

  @Test
  public void ordenEntregadaEsGris() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setEstado(EstadoOrden.ENTREGADO);
    orden.setFechaIngreso(LocalDateTime.now().minusDays(10));

    assertThat(orden.getColorPrioridad(), is("gris"));
  }
}
