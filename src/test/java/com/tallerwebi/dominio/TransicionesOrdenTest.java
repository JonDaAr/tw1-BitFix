package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TransicionesOrdenTest {

  private RepositorioOrdenReparacion repositorio;
  private ServicioOrdenReparacionImpl servicio;
  private OrdenReparacion orden;

  @BeforeEach
  public void preparar() {
    repositorio = mock(RepositorioOrdenReparacion.class);
    servicio = new ServicioOrdenReparacionImpl(mock(RepositorioUsuario.class), repositorio);
    orden = new OrdenReparacion();
    orden.setNotaTecnica("Nota original");
    when(repositorio.buscarPorId(1L)).thenReturn(orden);
  }

  @Test
  public void noPermiteSaltarDesdeRecepcionHastaEntrega() {
    orden.setMontoTotal(25000.0);
    assertThrows(IllegalStateException.class,
      () -> servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.ENTREGADO, "Nota adulterada"));
    assertEquals(EstadoOrden.RECIBIDO, orden.getEstado());
    assertEquals("Nota original", orden.getNotaTecnica());
    assertNull(orden.getFechaEntrega());
  }

  @Test
  public void permiteElFlujoDeDiagnosticoYReparacionExistente() {
    servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.EN_DIAGNOSTICO, "Diagnóstico");
    servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.ESPERANDO_REPUESTO, "Esperando pantalla");
    servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.EN_REPARACION, "Reparando");
    servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.REPARADO, "Reparación terminada");
    assertEquals(EstadoOrden.REPARADO, orden.getEstado());
    assertEquals("Reparación terminada", orden.getNotaTecnica());
  }

  @Test
  public void entregaRequiereMontoPositivoYFinitoEnAmbosServicios() {
    ServicioCierreOrdenImpl cierre = new ServicioCierreOrdenImpl(repositorio);
    assertThrows(IllegalArgumentException.class, () -> orden.setMontoTotal(Double.NaN));
    assertThrows(IllegalArgumentException.class, () -> orden.setMontoTotal(Double.POSITIVE_INFINITY));
    for (Double monto : new Double[] { null, 0.0, -1.0 }) {
      orden.setEstado(EstadoOrden.REPARADO);
      orden.setMontoTotal(monto);
      assertThrows(IllegalStateException.class,
        () -> servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.ENTREGADO, "Nueva nota"));
      assertThrows(com.tallerwebi.dominio.excepcion.EstadoInvalidoParaCierreException.class,
        () -> cierre.cerrarOrden(1L));
      assertEquals(EstadoOrden.REPARADO, orden.getEstado());
      assertNull(orden.getFechaEntrega());
      assertEquals("Nota original", orden.getNotaTecnica());
    }
  }

  @Test
  public void entregaValidaRegistraFechaYUnaEdicionPosteriorLaConserva() {
    orden.setEstado(EstadoOrden.REPARADO);
    orden.setMontoTotal(25000.0);
    servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.ENTREGADO, "Entregado");
    assertNotNull(orden.getFechaEntrega());
    LocalDateTime fechaOriginal = LocalDateTime.of(2026, 1, 1, 12, 0);
    orden.setFechaEntrega(fechaOriginal);
    servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.ENTREGADO, "Nota adicional");
    assertEquals(fechaOriginal, orden.getFechaEntrega());
    assertEquals("Nota adicional", orden.getNotaTecnica());
  }

  @Test
  public void estadosFinalesNoPermitenReabrirLaOrden() {
    for (EstadoOrden estado : new EstadoOrden[] {
      EstadoOrden.ENTREGADO, EstadoOrden.COTIZACION_DESCARTADA, EstadoOrden.PRESUPUESTO_RECHAZADO
    }) {
      orden.setEstado(estado);
      assertThrows(IllegalStateException.class,
        () -> servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.EN_REPARACION, "Reabrir"));
      assertEquals(estado, orden.getEstado());
    }
  }

  @Test
  public void cotizacionSoloPuedeAceptarseCuandoFueEnviada() {
    orden.setEstado(EstadoOrden.SOLICITUD_COTIZACION);
    assertThrows(IllegalStateException.class,
      () -> servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.ESPERANDO_INGRESO, null));
    servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.COTIZACION_PREVIA_ENVIADA, null);
    servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.ESPERANDO_INGRESO, null);
    servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.RECIBIDO, null);
    assertEquals(EstadoOrden.RECIBIDO, orden.getEstado());
  }

  @Test
  public void noPermiteSimularAceptacionDePresupuestoDesdeLaEdicion() {
    orden.setEstado(EstadoOrden.PRESUPUESTO_ENVIADO);
    assertThrows(IllegalStateException.class,
      () -> servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.PRESUPUESTO_ACEPTADO, null));
    assertEquals(EstadoOrden.PRESUPUESTO_ENVIADO, orden.getEstado());
  }

  @Test
  public void estadosNulosYOrdenInexistenteProducenErroresControlados() {
    assertThrows(IllegalStateException.class,
      () -> servicio.actualizarEstadoYNotaTecnica(1L, null, "Nota"));
    orden.setEstado(null);
    assertThrows(IllegalStateException.class,
      () -> servicio.actualizarEstadoYNotaTecnica(1L, EstadoOrden.RECIBIDO, "Nota"));
    assertThrows(IllegalArgumentException.class,
      () -> servicio.actualizarEstadoYNotaTecnica(99L, EstadoOrden.RECIBIDO, "Nota"));
    assertEquals("Nota original", orden.getNotaTecnica());
  }
}
