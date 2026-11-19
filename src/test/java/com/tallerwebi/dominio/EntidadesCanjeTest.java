package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class EntidadesCanjeTest {

  @Test
  public void computadoraUsadaExponeSusDatos() {
    ComputadoraUsada computadora = new ComputadoraUsada();
    computadora.setId(1L);
    computadora.setMarca("Lenovo");
    computadora.setModelo("ThinkPad T480");
    computadora.setDescripcion("SSD y batería nuevos");
    computadora.setCondicion("Reacondicionada");
    computadora.setPrecio(175000.0);
    computadora.setStock(2);
    computadora.setActiva(false);

    assertEquals(1L, computadora.getId());
    assertEquals("Lenovo", computadora.getMarca());
    assertEquals("ThinkPad T480", computadora.getModelo());
    assertEquals("SSD y batería nuevos", computadora.getDescripcion());
    assertEquals("Reacondicionada", computadora.getCondicion());
    assertEquals(175000.0, computadora.getPrecio());
    assertEquals(2, computadora.getStock());
    assertFalse(computadora.getActiva());
  }

  @Test
  public void solicitudCanjeExponeSusDatosYEstadoInicial() {
    SolicitudCanje solicitud = new SolicitudCanje();
    Usuario cliente = new Usuario();
    ComputadoraUsada computadora = new ComputadoraUsada();
    LocalDateTime fecha = LocalDateTime.of(2026, 11, 11, 12, 30);

    assertEquals(EstadoSolicitudCanje.PENDIENTE, solicitud.getEstado());
    assertTrue(solicitud.getFechaSolicitud() != null);

    solicitud.setId(4L);
    solicitud.setCliente(cliente);
    solicitud.setComputadora(computadora);
    solicitud.setEquipoMarca("HP");
    solicitud.setEquipoModelo("Pavilion");
    solicitud.setEquipoDescripcion("No enciende");
    solicitud.setValorOfrecido(25000.0);
    solicitud.setValorAprobado(30000.0);
    solicitud.setEstado(EstadoSolicitudCanje.APROBADA);
    solicitud.setFechaSolicitud(fecha);

    assertEquals(4L, solicitud.getId());
    assertEquals(cliente, solicitud.getCliente());
    assertEquals(computadora, solicitud.getComputadora());
    assertEquals("HP", solicitud.getEquipoMarca());
    assertEquals("Pavilion", solicitud.getEquipoModelo());
    assertEquals("No enciende", solicitud.getEquipoDescripcion());
    assertEquals(25000.0, solicitud.getValorOfrecido());
    assertEquals(30000.0, solicitud.getValorAprobado());
    assertEquals(EstadoSolicitudCanje.APROBADA, solicitud.getEstado());
    assertEquals(fecha, solicitud.getFechaSolicitud());
    assertNull(new SolicitudCanje().getValorAprobado());
  }

  @Test
  public void estadoSolicitudIncluyeEstadosDelFlujo() {
    assertEquals(4, EstadoSolicitudCanje.values().length);
    assertEquals(EstadoSolicitudCanje.CONTRAOFERTA, EstadoSolicitudCanje.valueOf("CONTRAOFERTA"));
    assertEquals(EstadoSolicitudCanje.RECHAZADA, EstadoSolicitudCanje.valueOf("RECHAZADA"));
  }
}
