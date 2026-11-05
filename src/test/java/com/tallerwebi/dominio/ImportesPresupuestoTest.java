package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.presentacion.ItemPresupuestoForm;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ImportesPresupuestoTest {

  @Test
  public void calculosDecimalesYSuTotalPersistidoConservanCentavosExactos() {
    RepositorioRepuesto repuestos = mock(RepositorioRepuesto.class);
    RepositorioOrdenReparacion ordenes = mock(RepositorioOrdenReparacion.class);
    ServicioPresupuestoImpl servicio = new ServicioPresupuestoImpl(repuestos, ordenes);
    when(repuestos.buscarPorId(1L)).thenReturn(new Repuesto("Primero", 0.10, 5));
    when(repuestos.buscarPorId(2L)).thenReturn(new Repuesto("Segundo", 0.20, 5));
    OrdenReparacion orden = new OrdenReparacion();
    when(ordenes.buscarPorCodigo(123456)).thenReturn(orden);
    List<ItemPresupuestoForm> items = List.of(new ItemPresupuestoForm(1L, 1), new ItemPresupuestoForm(2L, 1));
    assertEquals(0.30, servicio.calcularSubtotalRepuestos(items));
    assertEquals(20000.30, servicio.calcularTotalPresupuesto(items));
    servicio.enviarPresupuesto(123456, items, 99999.0);
    assertEquals(20000.30, orden.getMontoTotal());
    assertEquals(0.30, orden.getSubtotalRepuestos());
    assertEquals(20000.0, orden.getCostoManoDeObra());
  }

  @Test
  public void importesSeRedondeanADosDecimalesYRechazanNoFinitos() {
    Repuesto repuesto = new Repuesto("Prueba", 12.345, 1);
    assertEquals(12.35, repuesto.getPrecio());
    assertThrows(IllegalArgumentException.class, () -> repuesto.setPrecio(Double.NaN));
    assertThrows(IllegalArgumentException.class, () -> repuesto.setPrecio(Double.POSITIVE_INFINITY));
  }
}
