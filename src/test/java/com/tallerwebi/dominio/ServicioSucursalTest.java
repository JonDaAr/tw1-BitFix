package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioSucursalTest {

  private RepositorioSucursal repositorioSucursalMock;
  private ServicioSucursal servicioSucursal;

  @BeforeEach
  public void init() {
    this.repositorioSucursalMock = mock(RepositorioSucursal.class);
    this.servicioSucursal = new ServicioSucursalImpl(this.repositorioSucursalMock);
  }

  @Test
  public void dadoQueExistenSucursalesCuandoSePidenCercanasConLatYLon_entoncesCalculaDistanciaYOrdenaPorCercania() {
    // Given
    Sucursal sucursalLejana = new Sucursal();
    sucursalLejana.setNombre("Sucursal Palermo");
    sucursalLejana.setLatitud(-34.5885);
    sucursalLejana.setLongitud(-58.4103);

    Sucursal sucursalCercana = new Sucursal();
    sucursalCercana.setNombre("Sucursal Centro");
    sucursalCercana.setLatitud(-34.6037);
    sucursalCercana.setLongitud(-58.3816);

    // El repositorio devuelve ambas (desordenadas o en cualquier orden)
    when(this.repositorioSucursalMock.obtenerTodas())
      .thenReturn(Arrays.asList(sucursalLejana, sucursalCercana));

    // Coordenadas de referencia simuladas (cerca del Obelisco, más próxima a "Sucursal Centro")
    Double latRef = -34.6030;
    Double lonRef = -58.3810;

    // When
    List<Sucursal> resultado = this.servicioSucursal.obtenerSucursalesCercanas(latRef, lonRef);

    // Then
    assertThat(resultado, notNullValue());
    assertThat(resultado, hasSize(2));
    // La primera debe ser la más cercana calculada por Haversine
    assertThat(resultado.get(0).getNombre(), equalTo("Sucursal Centro"));
    assertThat(resultado.get(0).getDistancia(), notNullValue());
    assertThat(resultado.get(1).getNombre(), equalTo("Sucursal Palermo"));

    verify(this.repositorioSucursalMock, times(1)).obtenerTodas();
  }

  @Test
  public void dadoQueNoSeProporcionanCoordenadasCuandoSePidenSucursales_entoncesDevuelveLaListaSinCalcularDistancia() {
    // Given
    Sucursal sucursal1 = new Sucursal();
    sucursal1.setNombre("Sucursal San Justo");

    Sucursal sucursal2 = new Sucursal();
    sucursal2.setNombre("Sucursal Ramos Mejía");

    when(this.repositorioSucursalMock.obtenerTodas())
      .thenReturn(Arrays.asList(sucursal1, sucursal2));

    // When (lat y lon en null)
    List<Sucursal> resultado = this.servicioSucursal.obtenerSucursalesCercanas(null, null);

    // Then
    assertThat(resultado, notNullValue());
    assertThat(resultado, hasSize(2));
    assertThat(resultado.get(0).getDistancia(), nullValue());

    verify(this.repositorioSucursalMock, times(1)).obtenerTodas();
  }
}
