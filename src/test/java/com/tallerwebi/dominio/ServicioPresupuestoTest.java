package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.SinStockException;
import com.tallerwebi.presentacion.ItemPresupuestoForm;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioPresupuestoTest {

  private RepositorioRepuesto repositorioRepuestoMock;
  private RepositorioOrdenReparacion repositorioOrdenMock; // <- 1. Declarar el segundo mock
  private ServicioPresupuesto servicioPresupuesto;

  @BeforeEach
  public void init() {
    this.repositorioRepuestoMock = mock(RepositorioRepuesto.class);
    this.repositorioOrdenMock = mock(RepositorioOrdenReparacion.class); // <- 2. Inicializarlo

    this.servicioPresupuesto =
      new ServicioPresupuestoImpl(this.repositorioRepuestoMock, this.repositorioOrdenMock);
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
  public void dadoQueNoHayStockSuficiente_quandoSeCalculaSubtotal_entoncesLanzaSinStockException() {
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

  @Test
  public void queAlCalcularTotalConVariosItemsRetorneLaSumaCorrecta() throws SinStockException {
    // Dado
    Repuesto ram = new Repuesto();
    ram.setId(1L);
    ram.setNombre("Memoria RAM 16GB");
    ram.setPrecio(38000.0);
    ram.setStock(5);

    Repuesto ssd = new Repuesto();
    ssd.setId(2L);
    ssd.setNombre("SSD NVMe 1TB");
    ssd.setPrecio(52000.0);
    ssd.setStock(3);

    when(repositorioRepuestoMock.buscarPorId(1L)).thenReturn(ram);
    when(repositorioRepuestoMock.buscarPorId(2L)).thenReturn(ssd);

    List<ItemPresupuestoForm> items = new ArrayList<>();
    items.add(new ItemPresupuestoForm(1L, 2)); // 2 x 38000 = 76000
    items.add(new ItemPresupuestoForm(2L, 1)); // 1 x 52000 = 52000

    // Cuando
    Double total = servicioPresupuesto.calcularTotalPresupuesto(items);

    // Entonces
    assertThat(total, equalTo(172800.0));
  }

  @Test
  public void queAlCalcularTotalConListaVaciaRetorneCero() throws SinStockException {
    // Dado
    List<ItemPresupuestoForm> itemsVacios = new ArrayList<>();

    // Cuando
    Double total = servicioPresupuesto.calcularTotalPresupuesto(itemsVacios);

    // Entonces
    assertThat(total, equalTo(0.0));
  }

  @Test
  public void queLanceSinStockExceptionSiAlgunItemSuperaElStockDisponible() {
    // Dado
    Repuesto pantalla = new Repuesto();
    pantalla.setId(3L);
    pantalla.setNombre("Pantalla OLED");
    pantalla.setPrecio(18000.0);
    pantalla.setStock(1);

    when(repositorioRepuestoMock.buscarPorId(3L)).thenReturn(pantalla);

    List<ItemPresupuestoForm> items = new ArrayList<>();
    items.add(new ItemPresupuestoForm(3L, 5));

    // Cuando y Entonces
    org.junit.jupiter.api.Assertions.assertThrows(
      SinStockException.class,
      () -> {
        servicioPresupuesto.calcularTotalPresupuesto(items);
      }
    );
  }

  @Test
  public void cuandoSubtotalRepuestosEsCero_laManoDeObraDebeSerCero() {
    Double manoDeObra = servicioPresupuesto.calcularManoDeObra(0.0);
    assertThat(manoDeObra, equalTo(0.0));
  }

  @Test
  public void cuandoSubtotalRepuestosEsMenorOIgualA50000_aplicaPisoFijoDe20000() {
    Double manoDeObraMenor = servicioPresupuesto.calcularManoDeObra(30000.0);
    Double manoDeObraExacto = servicioPresupuesto.calcularManoDeObra(50000.0);

    assertThat(manoDeObraMenor, equalTo(20000.0));
    assertThat(manoDeObraExacto, equalTo(20000.0));
  }

  @Test
  public void cuandoSubtotalRepuestosEstaEntre50001Y150000_aplicaTreintaYCincoPorCiento() {
    Double manoDeObra = servicioPresupuesto.calcularManoDeObra(100000.0);
    assertThat(manoDeObra, equalTo(35000.0));
  }

  @Test
  public void cuandoSubtotalRepuestosSupera150000SinLlegarAlTope_aplicaVeinticincoPorCiento() {
    Double manoDeObra = servicioPresupuesto.calcularManoDeObra(200000.0);
    assertThat(manoDeObra, equalTo(50000.0));
  }

  @Test
  public void cuandoManoDeObraCalculadaSupera60000_aplicaTopeMaximo() {
    Double manoDeObra = servicioPresupuesto.calcularManoDeObra(400000.0);
    assertThat(manoDeObra, equalTo(60000.0));
  }
}
