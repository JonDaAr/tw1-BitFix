package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioLogisticaTest {

  private RepositorioSucursal repositorioSucursalMock;
  private ServicioLogistica servicioLogistica;

  @BeforeEach
  public void init() {
    repositorioSucursalMock = mock(RepositorioSucursal.class);
    servicioLogistica = new ServicioLogisticaImpl(repositorioSucursalMock);
  }

  @Test
  public void siNoHaySucursalesDeberiaRetornarViableFalse() {
    when(repositorioSucursalMock.obtenerTodas()).thenReturn(new ArrayList<>());

    ResultadoCotizacionLogistica resultado = servicioLogistica.cotizarRetiro(-34.0, -58.0);

    assertThat(resultado.isViable(), is(false));
    assertThat(
      resultado.getMensajeError(),
      equalTo("No hay sucursales operativas en este momento.")
    );
  }

  @Test
  public void siLaSucursalEstaMuyLejosDeberiaRetornarViableFalse() {
    Sucursal sucursalEnCaba = new Sucursal();
    sucursalEnCaba.setLatitud(-34.6037); // Obelisco
    sucursalEnCaba.setLongitud(-58.3816);
    when(repositorioSucursalMock.obtenerTodas()).thenReturn(List.of(sucursalEnCaba));

    // Cliente pide desde muy lejos (> 40km)
    ResultadoCotizacionLogistica resultado = servicioLogistica.cotizarRetiro(-34.4587, -58.9142);

    assertThat(resultado.isViable(), is(false));
    assertThat(
      resultado.getMensajeError(),
      containsString("Supera nuestra área de cobertura máxima")
    );
  }

  @Test
  public void siLaSucursalEstaEnRangoDeberiaCalcularElCostoYSerViable() {
    Sucursal sucursalLocal = new Sucursal();
    sucursalLocal.setLatitud(-34.6531);
    sucursalLocal.setLongitud(-58.6206);
    when(repositorioSucursalMock.obtenerTodas()).thenReturn(List.of(sucursalLocal));

    // Cliente está a pocos km (ej: en Ramos Mejía)
    ResultadoCotizacionLogistica resultado = servicioLogistica.cotizarRetiro(-34.6400, -58.5600);

    assertThat(resultado.isViable(), is(true));
    assertThat(resultado.getSucursalAsignada(), equalTo(sucursalLocal));
    assertThat(resultado.getDistanciaKm(), notNullValue());

    // Debe haber sumado el Costo Base (2500) + (distancia * 600)
    assertThat(resultado.getCostoTotal(), greaterThan(2500.0));
  }
}
