package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ResultadoCotizacionLogistica;
import com.tallerwebi.dominio.ServicioLogistica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ControladorLogisticaTest {

  private ServicioLogistica servicioLogisticaMock;
  private ControladorLogistica controladorLogistica;

  @BeforeEach
  public void init() {
    servicioLogisticaMock = mock(ServicioLogistica.class);
    controladorLogistica = new ControladorLogistica(servicioLogisticaMock);
  }

  @Test
  public void cotizarEnvioDeberiaRetornarElResultadoDelServicioEnFormatoJson() {
    Double latitudCliente = -34.64;
    Double longitudCliente = -58.56;

    ResultadoCotizacionLogistica mockResultado = new ResultadoCotizacionLogistica();
    mockResultado.setViable(true);
    mockResultado.setCostoTotal(3500.0);
    mockResultado.setDistanciaKm(1.5);

    when(servicioLogisticaMock.cotizarRetiro(latitudCliente, longitudCliente))
      .thenReturn(mockResultado);

    ResultadoCotizacionLogistica resultado = controladorLogistica.cotizarEnvio(
      latitudCliente,
      longitudCliente
    );

    assertThat(resultado, notNullValue());
    assertThat(resultado.isViable(), is(true));
    assertThat(resultado.getCostoTotal(), equalTo(3500.0));

    verify(servicioLogisticaMock, times(1)).cotizarRetiro(latitudCliente, longitudCliente);
  }
}
