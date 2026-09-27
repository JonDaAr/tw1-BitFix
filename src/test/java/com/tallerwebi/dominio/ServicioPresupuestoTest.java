package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.SinStockException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioPresupuestoTest {

  private RepositorioRepuesto repositorioRepuestoMock;
  private ServicioPresupuesto servicioPresupuesto;

  @BeforeEach
  public void init() {
    this.repositorioRepuestoMock = mock(RepositorioRepuesto.class);
    this.servicioPresupuesto = new ServicioPresupuestoImpl(this.repositorioRepuestoMock);
  }

  @Test
  public void dadoQueHayStock_cuandoSeCalculaSubtotal_entoncesDevuelveElMontoMultiplicado()
    throws SinStockException {
    // Given
    Repuesto repuesto = new Repuesto("Memoria RAM 8GB", 20000.0, 5);
    when(this.repositorioRepuestoMock.buscarPorId(1L)).thenReturn(repuesto);

    // When
    Double subtotal = this.servicioPresupuesto.calcularSubtotalRepuesto(1L, 2);

    // Then
    assertThat(subtotal, equalTo(40000.0));
    verify(this.repositorioRepuestoMock, times(1)).buscarPorId(1L);
  }

  @Test
  public void dadoQueNoHayStockSuficiente_cuandoSeCalculaSubtotal_entoncesLanzaSinStockException() {
    // Given
    Repuesto repuesto = new Repuesto("Placa de Video", 150000.0, 1);
    when(this.repositorioRepuestoMock.buscarPorId(2L)).thenReturn(repuesto);

    // When & Then (intenta pedir 3 cuando solo hay 1)
    assertThrows(
      SinStockException.class,
      () -> {
        this.servicioPresupuesto.calcularSubtotalRepuesto(2L, 3);
      }
    );
  }

  @Test
  public void dadoQueHayStock_cuandoSeDescuenta_entoncesGuardaElRepuestoConMenorCantidad()
    throws SinStockException {
    // Given
    Repuesto repuesto = new Repuesto("Pasta Térmica", 5000.0, 10);
    when(this.repositorioRepuestoMock.buscarPorId(3L)).thenReturn(repuesto);

    // When
    this.servicioPresupuesto.descontarStockRepuesto(3L, 4);

    // Then
    assertThat(repuesto.getStock(), equalTo(6));
    verify(this.repositorioRepuestoMock, times(1)).guardar(repuesto);
  }
}
