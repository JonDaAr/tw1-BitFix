package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ServicioSucursal;
import com.tallerwebi.dominio.Sucursal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorSucursalTest {

  private ServicioSucursal servicioSucursalMock;
  private ControladorSucursal controladorSucursal;

  @BeforeEach
  public void init() {
    this.servicioSucursalMock = mock(ServicioSucursal.class);
    this.controladorSucursal = new ControladorSucursal(this.servicioSucursalMock);
  }

  @Test
  public void cuandoSeListanSucursalesConParametrosDeUbicacionDebeInvocarAlServicioYRetornarVistaConModelo() {
    // Given
    Double lat = -34.6037;
    Double lon = -58.3816;

    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("Sucursal Centro");
    sucursal.setDistancia(1.5);

    List<Sucursal> sucursalesMock = Arrays.asList(sucursal);

    when(this.servicioSucursalMock.obtenerSucursalesCercanas(lat, lon)).thenReturn(sucursalesMock);

    // When
    ModelAndView mav = this.controladorSucursal.listarSucursales(lat, lon);

    // Then
    assertThat(mav.getViewName(), equalTo("sucursales"));
    assertThat(mav.getModel(), notNullValue());
    assertThat(mav.getModel().get("sucursales"), equalTo(sucursalesMock));
    assertThat(mav.getModel().get("latReferencia"), equalTo(lat));
    assertThat(mav.getModel().get("lonReferencia"), equalTo(lon));

    verify(this.servicioSucursalMock, times(1)).obtenerSucursalesCercanas(lat, lon);
  }

  @Test
  public void cuandoSeListanSucursalesSinParametrosDeUbicacionDebeRetornarVistaConValoresNulosEnElModelo() {
    // Given
    List<Sucursal> sucursalesMock = Arrays.asList(new Sucursal());

    when(this.servicioSucursalMock.obtenerSucursalesCercanas(null, null))
      .thenReturn(sucursalesMock);

    // When
    ModelAndView mav = this.controladorSucursal.listarSucursales(null, null);

    // Then
    assertThat(mav.getViewName(), equalTo("sucursales"));
    assertThat(mav.getModel(), notNullValue());
    assertThat(mav.getModel().get("sucursales"), equalTo(sucursalesMock));
    assertThat(mav.getModel().get("latReferencia"), nullValue());
    assertThat(mav.getModel().get("lonReferencia"), nullValue());

    verify(this.servicioSucursalMock, times(1)).obtenerSucursalesCercanas(null, null);
  }
}
