package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCotizacionPreviaTest {

  private RepositorioOrdenReparacion repositorioOrdenReparacionMock;
  private ServicioHistorialOrden servicioHistorialOrdenMock;
  private ServicioCotizacionPrevia servicioCotizacionPrevia;

  @BeforeEach
  public void init() {
    repositorioOrdenReparacionMock = mock(RepositorioOrdenReparacion.class);
    servicioHistorialOrdenMock = mock(ServicioHistorialOrden.class);
    servicioCotizacionPrevia =
      new ServicioCotizacionPreviaImpl(repositorioOrdenReparacionMock, servicioHistorialOrdenMock);
  }

  @Test
  public void enviarCotizacionPreviaGuardaMontoObservacionesEstadoEHistorial() {
    Long idOrden = 7L;
    OrdenReparacion orden = new OrdenReparacion();
    orden.setEstado(EstadoOrden.PENDIENTE_COTIZACION);
    when(repositorioOrdenReparacionMock.buscarPorId(idOrden)).thenReturn(orden);

    servicioCotizacionPrevia.enviarCotizacionPrevia(
      idOrden,
      25000.0,
      " El monto depende de revisar el equipo en el taller. "
    );

    assertEquals(25000.0, orden.getMontoTotal(), 0.0);
    assertEquals("El monto depende de revisar el equipo en el taller.", orden.getNotaTecnica());
    assertEquals(EstadoOrden.COTIZACION_PREVIA_ENVIADA, orden.getEstado());
    verify(servicioHistorialOrdenMock)
      .registrarEvento(orden, EstadoOrden.COTIZACION_PREVIA_ENVIADA);
    verify(repositorioOrdenReparacionMock).modificarOrden(orden);
  }

  @Test
  public void enviarCotizacionPreviaRequiereObservacionesNoNulas() {
    assertThrows(
      IllegalArgumentException.class,
      () -> servicioCotizacionPrevia.enviarCotizacionPrevia(7L, 25000.0, null)
    );
    verify(repositorioOrdenReparacionMock, never()).buscarPorId(anyLong());
  }

  @Test
  public void aceptarCotizacionPreviaActualizaEstadoEHistorial() {
    Integer codigo = 123456;
    String email = "cliente@mail.com";
    OrdenReparacion orden = ordenConCotizacionEnviada(email);
    when(repositorioOrdenReparacionMock.buscarPorCodigo(codigo)).thenReturn(orden);

    servicioCotizacionPrevia.aceptarCotizacionPrevia(codigo, email);

    assertEquals(EstadoOrden.ESPERANDO_INGRESO, orden.getEstado());
    verify(servicioHistorialOrdenMock).registrarEvento(orden, EstadoOrden.ESPERANDO_INGRESO);
    verify(repositorioOrdenReparacionMock).modificarOrden(orden);
  }

  @Test
  public void rechazarCotizacionPreviaActualizaEstadoEHistorial() {
    Integer codigo = 123456;
    String email = "cliente@mail.com";
    OrdenReparacion orden = ordenConCotizacionEnviada(email);
    when(repositorioOrdenReparacionMock.buscarPorCodigo(codigo)).thenReturn(orden);

    servicioCotizacionPrevia.rechazarCotizacionPrevia(codigo, email);

    assertEquals(EstadoOrden.COTIZACION_DESCARTADA, orden.getEstado());
    verify(servicioHistorialOrdenMock).registrarEvento(orden, EstadoOrden.COTIZACION_DESCARTADA);
    verify(repositorioOrdenReparacionMock).modificarOrden(orden);
  }

  @Test
  public void responderCotizacionPreviaRechazaEmailDeOtroUsuario() {
    OrdenReparacion orden = ordenConCotizacionEnviada("dueño@mail.com");
    when(repositorioOrdenReparacionMock.buscarPorCodigo(123456)).thenReturn(orden);

    assertThrows(
      SecurityException.class,
      () -> servicioCotizacionPrevia.aceptarCotizacionPrevia(123456, "intruso@mail.com")
    );
    verify(repositorioOrdenReparacionMock, never()).modificarOrden(orden);
  }

  @Test
  public void responderCotizacionPreviaRequiereEstadoEnviado() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setEmailCliente("cliente@mail.com");
    orden.setEstado(EstadoOrden.PENDIENTE_COTIZACION);
    when(repositorioOrdenReparacionMock.buscarPorCodigo(123456)).thenReturn(orden);

    assertThrows(
      IllegalStateException.class,
      () -> servicioCotizacionPrevia.aceptarCotizacionPrevia(123456, "cliente@mail.com")
    );
    verify(repositorioOrdenReparacionMock, never()).modificarOrden(orden);
  }

  private OrdenReparacion ordenConCotizacionEnviada(String email) {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setEmailCliente(email);
    orden.setEstado(EstadoOrden.COTIZACION_PREVIA_ENVIADA);
    return orden;
  }
}
