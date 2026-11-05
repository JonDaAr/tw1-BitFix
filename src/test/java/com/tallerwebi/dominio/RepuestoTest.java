package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RepuestoTest {

  @Test
  public void tieneStockSuficienteDeberiaRetornarTrueSiElStockEsMayorOIgual() {
    Repuesto repuesto = new Repuesto("Pantalla LCD", 15000.0, 5);

    assertTrue(repuesto.tieneStockSuficiente(3));
    assertTrue(repuesto.tieneStockSuficiente(5));
  }

  @Test
  public void tieneStockSuficienteDeberiaRetornarFalseSiElStockEsInsuficiente() {
    Repuesto repuesto = new Repuesto("Pantalla LCD", 15000.0, 2);

    assertFalse(repuesto.tieneStockSuficiente(5));
  }

  @Test
  public void queDescontarStockReduzcaCorrectamenteElStock() {
    Repuesto repuesto = new Repuesto("Batería", 8000.0, 10);

    repuesto.descontarStock(4);

    assertThat(repuesto.getStock(), equalTo(6));
  }
}
