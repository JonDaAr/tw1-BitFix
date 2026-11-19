package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioHistorialOrdenTest {

  private RepositorioHistorialOrden repositorioMock;
  private ServicioHistorialOrden servicio;

  @BeforeEach
  public void init() {
    repositorioMock = mock(RepositorioHistorialOrden.class);
    servicio = new ServicioHistorialOrdenImpl(repositorioMock);
  }

  @Test
  public void queDevuelvaElHistorialDeUnaOrden() {
    HistorialOrden evento = new HistorialOrden(null, EstadoOrden.RECIBIDO);
    when(repositorioMock.buscarPorOrden(1L)).thenReturn(List.of(evento));

    List<HistorialOrden> historial = servicio.obtenerHistorial(1L);

    assertThat(historial, hasSize(1));
    assertThat(historial.get(0).getEstado(), is(equalTo(EstadoOrden.RECIBIDO)));
  }

  @Test
  public void queGuardeUnEventoAlRegistrarlo() {
    OrdenReparacion orden = new OrdenReparacion();

    servicio.registrarEvento(orden, EstadoOrden.EN_DIAGNOSTICO);

    verify(repositorioMock).guardar(any(HistorialOrden.class));
  }
}
