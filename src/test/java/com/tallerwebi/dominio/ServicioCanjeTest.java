package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCanjeTest {

  private RepositorioCanje repositorio;
  private ServicioCanje servicio;
  private Usuario cliente;
  private ComputadoraUsada computadora;

  @BeforeEach
  public void init() {
    repositorio = mock(RepositorioCanje.class);
    servicio = new ServicioCanjeImpl(repositorio);
    cliente = new Usuario();
    cliente.setId(7L);
    cliente.setEmail("cliente@bitfix.com");
    computadora = computadoraValida();
    computadora.setId(15L);
  }

  @Test
  public void listarConsultasDeleganEnRepositorio() {
    List<ComputadoraUsada> disponibles = List.of(computadora);
    List<ComputadoraUsada> todas = List.of(computadora);
    List<SolicitudCanje> solicitudes = List.of(new SolicitudCanje());
    when(repositorio.listarComputadorasDisponibles()).thenReturn(disponibles);
    when(repositorio.listarComputadoras()).thenReturn(todas);
    when(repositorio.listarSolicitudes()).thenReturn(solicitudes);
    when(repositorio.listarSolicitudesDeCliente(7L)).thenReturn(solicitudes);

    assertEquals(disponibles, servicio.listarDisponibles());
    assertEquals(todas, servicio.listarTodasLasComputadoras());
    assertEquals(solicitudes, servicio.listarSolicitudes());
    assertEquals(solicitudes, servicio.listarSolicitudesDeCliente(7L));
  }

  @Test
  public void guardarComputadoraValidaActivaLaNuevaYLaPersiste() {
    computadora.setId(null);
    computadora.setActiva(null);

    servicio.guardarComputadora(computadora);

    assertTrue(computadora.getActiva());
    verify(repositorio).guardarComputadora(computadora);
  }

  @Test
  public void guardarComputadoraConservaEstadoPausado() {
    computadora.setId(null);
    computadora.setActiva(false);

    servicio.guardarComputadora(computadora);

    assertFalse(computadora.getActiva());
    verify(repositorio).guardarComputadora(computadora);
  }

  @Test
  public void guardarComputadoraRechazaDatosInvalidos() {
    assertThrows(IllegalArgumentException.class, () -> servicio.guardarComputadora(null));
    computadora.setMarca(" ");
    assertThrows(IllegalArgumentException.class, () -> servicio.guardarComputadora(computadora));
    computadora = computadoraValida();
    computadora.setModelo(null);
    assertThrows(IllegalArgumentException.class, () -> servicio.guardarComputadora(computadora));
    computadora = computadoraValida();
    computadora.setCondicion("");
    assertThrows(IllegalArgumentException.class, () -> servicio.guardarComputadora(computadora));
    computadora = computadoraValida();
    computadora.setPrecio(null);
    assertThrows(IllegalArgumentException.class, () -> servicio.guardarComputadora(computadora));
    computadora = computadoraValida();
    computadora.setPrecio(0.0);
    assertThrows(IllegalArgumentException.class, () -> servicio.guardarComputadora(computadora));
    computadora = computadoraValida();
    computadora.setStock(null);
    assertThrows(IllegalArgumentException.class, () -> servicio.guardarComputadora(computadora));
    computadora = computadoraValida();
    computadora.setStock(-1);
    assertThrows(IllegalArgumentException.class, () -> servicio.guardarComputadora(computadora));
  }

  @Test
  public void pausarComputadoraDesactivaLaPublicacion() {
    when(repositorio.buscarComputadora(15L)).thenReturn(computadora);

    servicio.pausarComputadora(15L);

    assertFalse(computadora.getActiva());
  }

  @Test
  public void pausarComputadoraInexistenteLanzaError() {
    when(repositorio.buscarComputadora(15L)).thenReturn(null);

    assertThrows(IllegalArgumentException.class, () -> servicio.pausarComputadora(15L));
  }

  @Test
  public void solicitarCanjeRegistraSolicitudPendienteConDatosNormalizados() {
    when(repositorio.buscarComputadora(15L)).thenReturn(computadora);

    servicio.solicitarCanje(15L, cliente, "  Dell ", "  Latitude ", " No enciende ", 50000.0);

    org.mockito.ArgumentCaptor<SolicitudCanje> captor = org.mockito.ArgumentCaptor.forClass(
      SolicitudCanje.class
    );
    verify(repositorio).guardarSolicitud(captor.capture());
    SolicitudCanje solicitud = captor.getValue();
    assertEquals(cliente, solicitud.getCliente());
    assertEquals(computadora, solicitud.getComputadora());
    assertEquals("Dell", solicitud.getEquipoMarca());
    assertEquals("Latitude", solicitud.getEquipoModelo());
    assertEquals("No enciende", solicitud.getEquipoDescripcion());
    assertEquals(50000.0, solicitud.getValorOfrecido());
    assertEquals(EstadoSolicitudCanje.PENDIENTE, solicitud.getEstado());
    assertNotNull(solicitud.getFechaSolicitud());
  }

  @Test
  public void solicitarCanjePermiteOmitirValorEstimado() {
    when(repositorio.buscarComputadora(15L)).thenReturn(computadora);

    servicio.solicitarCanje(15L, cliente, "Dell", "Latitude", "Falla", null);

    org.mockito.ArgumentCaptor<SolicitudCanje> captor = org.mockito.ArgumentCaptor.forClass(
      SolicitudCanje.class
    );
    verify(repositorio).guardarSolicitud(captor.capture());
    assertNull(captor.getValue().getValorOfrecido());
  }

  @Test
  public void solicitarCanjeRechazaClienteODatosInvalidos() {
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.solicitarCanje(15L, null, "Dell", "Latitude", "Falla", null)
    );
    Usuario sinId = new Usuario();
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.solicitarCanje(15L, sinId, "Dell", "Latitude", "Falla", null)
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.solicitarCanje(15L, cliente, " ", "Latitude", "Falla", null)
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.solicitarCanje(15L, cliente, "Dell", null, "Falla", null)
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.solicitarCanje(15L, cliente, "Dell", "Latitude", " ", null)
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.solicitarCanje(15L, cliente, "Dell", "Latitude", "Falla", -1.0)
    );
  }

  @Test
  public void solicitarCanjeRechazaComputadoraInexistentePausadaOSinStock() {
    when(repositorio.buscarComputadora(15L)).thenReturn(null);
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.solicitarCanje(15L, cliente, "Dell", "Latitude", "Falla", null)
    );

    computadora.setActiva(false);
    when(repositorio.buscarComputadora(15L)).thenReturn(computadora);
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.solicitarCanje(15L, cliente, "Dell", "Latitude", "Falla", null)
    );

    computadora.setActiva(true);
    computadora.setStock(null);
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.solicitarCanje(15L, cliente, "Dell", "Latitude", "Falla", null)
    );
    computadora.setStock(0);
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.solicitarCanje(15L, cliente, "Dell", "Latitude", "Falla", null)
    );
  }

  @Test
  public void rechazarSolicitudPendienteActualizaElEstado() {
    SolicitudCanje solicitud = solicitudPendiente();
    when(repositorio.buscarSolicitud(22L)).thenReturn(solicitud);

    servicio.resolverSolicitud(22L, false, null);

    assertEquals(EstadoSolicitudCanje.RECHAZADA, solicitud.getEstado());
    assertNull(solicitud.getValorAprobado());
  }

  @Test
  public void resolverSolicitudRechazaSolicitudInexistenteOResuelta() {
    when(repositorio.buscarSolicitud(22L)).thenReturn(null);
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.resolverSolicitud(22L, false, null)
    );

    SolicitudCanje resuelta = solicitudPendiente();
    resuelta.setEstado(EstadoSolicitudCanje.APROBADA);
    when(repositorio.buscarSolicitud(22L)).thenReturn(resuelta);
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.resolverSolicitud(22L, false, null)
    );
  }

  @Test
  public void aprobarSolicitudValidaTasacionYDescuentaStock() {
    SolicitudCanje solicitud = solicitudPendiente();
    ComputadoraUsada inventario = computadoraValida();
    inventario.setId(15L);
    inventario.setStock(2);
    when(repositorio.buscarSolicitud(22L)).thenReturn(solicitud);
    when(repositorio.buscarComputadoraConBloqueo(15L)).thenReturn(inventario);

    servicio.resolverSolicitud(22L, true, 80000.0);

    assertEquals(1, inventario.getStock());
    assertEquals(EstadoSolicitudCanje.APROBADA, solicitud.getEstado());
    assertEquals(80000.0, solicitud.getValorAprobado());
    verify(repositorio).guardarComputadora(inventario);
  }

  @Test
  public void aprobarSolicitudRechazaTasacionInvalida() {
    SolicitudCanje solicitud = solicitudPendiente();
    when(repositorio.buscarSolicitud(22L)).thenReturn(solicitud);

    assertThrows(IllegalArgumentException.class, () -> servicio.resolverSolicitud(22L, true, null));
    assertThrows(IllegalArgumentException.class, () -> servicio.resolverSolicitud(22L, true, -1.0));
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.resolverSolicitud(22L, true, 200001.0)
    );
  }

  @Test
  public void aprobarSolicitudRechazaProductoNoDisponible() {
    SolicitudCanje solicitud = solicitudPendiente();
    when(repositorio.buscarSolicitud(22L)).thenReturn(solicitud);
    when(repositorio.buscarComputadoraConBloqueo(15L)).thenReturn(null);
    assertThrows(IllegalStateException.class, () -> servicio.resolverSolicitud(22L, true, 80000.0));

    computadora.setId(15L);
    computadora.setActiva(false);
    when(repositorio.buscarComputadoraConBloqueo(15L)).thenReturn(computadora);
    assertThrows(IllegalStateException.class, () -> servicio.resolverSolicitud(22L, true, 80000.0));

    computadora.setActiva(true);
    computadora.setStock(null);
    assertThrows(IllegalStateException.class, () -> servicio.resolverSolicitud(22L, true, 80000.0));
    computadora.setStock(0);
    assertThrows(IllegalStateException.class, () -> servicio.resolverSolicitud(22L, true, 80000.0));
  }

  private ComputadoraUsada computadoraValida() {
    ComputadoraUsada pc = new ComputadoraUsada();
    pc.setMarca("Dell");
    pc.setModelo("Latitude");
    pc.setDescripcion("Reacondicionada");
    pc.setCondicion("Muy buena");
    pc.setPrecio(200000.0);
    pc.setStock(1);
    pc.setActiva(true);
    return pc;
  }

  private SolicitudCanje solicitudPendiente() {
    SolicitudCanje solicitud = new SolicitudCanje();
    solicitud.setComputadora(computadora);
    solicitud.setCliente(cliente);
    solicitud.setEstado(EstadoSolicitudCanje.PENDIENTE);
    return solicitud;
  }
}
