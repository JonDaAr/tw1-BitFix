package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioInventarioTest {

  private RepositorioRepuesto repositorioRepuestoMock;
  private ServicioInventario servicioInventario;

  @BeforeEach
  public void init() {
    repositorioRepuestoMock = mock(RepositorioRepuesto.class);
    servicioInventario = new ServicioInventarioImpl(repositorioRepuestoMock);
  }

  @Test
  public void dadoQueExistenRepuestos_cuandoSeListanTodos_entoncesRetornaLaListaCompleta() {
    // Dado
    List<Repuesto> repuestos = new ArrayList<>();
    repuestos.add(new Repuesto());
    when(repositorioRepuestoMock.obtenerTodos()).thenReturn(repuestos);

    // Cuando
    List<Repuesto> resultado = servicioInventario.listarTodos();

    // Entonces
    assertThat(resultado.size(), equalTo(1));
    verify(repositorioRepuestoMock, times(1)).obtenerTodos();
  }

  @Test
  public void dadoUnRepuestoValido_cuandoSeGuarda_entoncesInvocaAlRepositorio() {
    // Dado
    Repuesto repuesto = new Repuesto();
    repuesto.setNombre("Módulo Display");
    repuesto.setPrecio(15000.0);
    repuesto.setStock(5);

    // Cuando
    servicioInventario.guardarOActualizar(repuesto);

    // Entonces
    verify(repositorioRepuestoMock, times(1)).guardar(repuesto);
  }

  @Test
  public void dadoUnRepuestoConPrecioNegativo_cuandoSeGuarda_entoncesLanzaIllegalArgumentException() {
    // Dado
    Repuesto repuesto = new Repuesto();
    repuesto.setNombre("Batería");
    repuesto.setPrecio(-500.0);
    repuesto.setStock(2);

    // Cuando y Entonces
    assertThrows(
      IllegalArgumentException.class,
      () -> {
        servicioInventario.guardarOActualizar(repuesto);
      }
    );
    verify(repositorioRepuestoMock, never()).guardar(any());
  }

  @Test
  public void dadoUnRepuestoConStockNegativo_cuandoSeGuarda_entoncesLanzaIllegalArgumentException() {
    // Dado
    Repuesto repuesto = new Repuesto();
    repuesto.setNombre("Pin de Carga");
    repuesto.setPrecio(2000.0);
    repuesto.setStock(-1);

    // Cuando y Entonces
    assertThrows(
      IllegalArgumentException.class,
      () -> {
        servicioInventario.guardarOActualizar(repuesto);
      }
    );
    verify(repositorioRepuestoMock, never()).guardar(any());
  }

  @Test
  public void dadoUnIdExistente_cuandoSeElimina_entoncesSeLlamaAEliminarEnRepositorio() {
    // Dado
    Long repuestoId = 1L;
    Repuesto repuestoExistente = new Repuesto();
    repuestoExistente.setId(repuestoId);

    when(repositorioRepuestoMock.buscarPorId(repuestoId)).thenReturn(repuestoExistente);

    // Cuando
    servicioInventario.eliminar(repuestoId);

    // Entonces
    verify(repositorioRepuestoMock, times(1)).eliminar(repuestoExistente);
  }
}
