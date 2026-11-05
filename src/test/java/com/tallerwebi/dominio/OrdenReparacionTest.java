package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;

public class OrdenReparacionTest {

  //TESTS PARA GETTERS Y SETTERS BASICOS

  @Test
  public void deberiaPermitirSetearYObtenerCamposBasicos() {
    OrdenReparacion orden = new OrdenReparacion();

    orden.setTelefonoCliente("1122334455");
    orden.setNivelReparacion(NivelReparacion.NIVEL_2);
    orden.setAccesorios("Cargador y funda");

    assertThat(orden.getTelefonoCliente(), equalTo("1122334455"));
    assertThat(orden.getNivelReparacion(), equalTo(NivelReparacion.NIVEL_2));
    assertThat(orden.getAccesorios(), equalTo("Cargador y funda"));
  }

  // --- TESTS PARA getCostoManoDeObra()

  @Test
  public void siMontoTotalEsNuloOCero_costoManoDeObraDebeSerCero() {
    OrdenReparacion ordenNula = new OrdenReparacion();
    ordenNula.setMontoTotal(null);

    OrdenReparacion ordenCero = new OrdenReparacion();
    ordenCero.setMontoTotal(0.0);

    assertThat(ordenNula.getCostoManoDeObra(), equalTo(0.0));
    assertThat(ordenCero.getCostoManoDeObra(), equalTo(0.0));
  }

  @Test
  public void siMontoTotalEsMenorOIgualAlTopeBaseSeDeberiaAplicatCostoManoDeObraEstimada() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setMontoTotal(30000.0);

    Double costo = orden.getCostoManoDeObra();
    assertThat(costo, notNullValue());
    assertThat(costo, greaterThan(0.0));
  }

  @Test
  public void siElMontoTotalEstaEnTramoMedioSeDeberiaCalculaCostoManoDeObraMedio() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setMontoTotal(100000.0);

    Double costo = orden.getCostoManoDeObra();
    assertThat(costo, notNullValue());
    assertThat(costo, greaterThan(0.0));
  }

  @Test
  public void siMontoTotalEsMuyAltoSeDeberiaAplicarTopeMaximoDeManoDeObraAlta() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setMontoTotal(500000.0);

    Double costo = orden.getCostoManoDeObra();
    assertThat(costo, notNullValue());
    assertThat(costo, greaterThan(0.0));
  }

  // --- TESTS PARA getSubtotalRepuestos() ---

  @Test
  public void siMontoTotalEsNuloOCeroElSubtotalRepuestosDeberiaSerCero() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setMontoTotal(null);

    assertThat(orden.getSubtotalRepuestos(), equalTo(0.0));

    orden.setMontoTotal(0.0);
    assertThat(orden.getSubtotalRepuestos(), equalTo(0.0));
  }

  @Test
  public void siMontoTotalTieneValorSeDeberiaCalcularSubtotalRepuestosRestandoManoDeObra() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setMontoTotal(120000.0);

    Double subtotalRepuestos = orden.getSubtotalRepuestos();

    assertThat(subtotalRepuestos, notNullValue());
    assertThat(subtotalRepuestos, greaterThanOrEqualTo(0.0));
  }
}
