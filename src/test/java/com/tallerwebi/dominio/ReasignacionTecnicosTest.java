package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReasignacionTecnicosTest {

  private RepositorioOrdenReparacion ordenes;
  private ServicioOrdenReparacionImpl servicio;
  private OrdenReparacion orden;
  private Usuario tecnico;

  @BeforeEach
  public void preparar() {
    ordenes = mock(RepositorioOrdenReparacion.class);
    RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
    servicio = new ServicioOrdenReparacionImpl(usuarios, ordenes);
    orden = new OrdenReparacion();
    tecnico = new Usuario();
    tecnico.setRol("TECNICO");
    tecnico.setActivo(true);
    tecnico.setNivelSoporte(NivelReparacion.NIVEL_3);
    when(ordenes.buscarPorId(1L)).thenReturn(orden);
    when(usuarios.buscarPorId(2L)).thenReturn(tecnico);
  }

  @Test
  public void reasignaATecnicoActivoConElNivelCorrecto() {
    servicio.reasignarOrden(1L, 2L);
    assertSame(tecnico, orden.getTecnicoAsignado());
    assertNotNull(orden.getFechaAsignacion());
    verify(ordenes).modificarOrden(orden);
  }

  @Test
  public void noReasignaAUsuariosInactivosClientesODeOtroNivel() {
    tecnico.setActivo(false);
    assertThrows(IllegalArgumentException.class, () -> servicio.reasignarOrden(1L, 2L));
    tecnico.setActivo(true);
    tecnico.setRol("CLIENTE");
    assertThrows(IllegalArgumentException.class, () -> servicio.reasignarOrden(1L, 2L));
    tecnico.setRol("TECNICO");
    tecnico.setNivelSoporte(NivelReparacion.NIVEL_1);
    assertThrows(IllegalArgumentException.class, () -> servicio.reasignarOrden(1L, 2L));
    assertNull(orden.getTecnicoAsignado());
    assertNull(orden.getFechaAsignacion());
    verify(ordenes, never()).modificarOrden(any());
  }

  @Test
  public void noReasignaOrdenesEntregadasRechazadasNiDescartadas() {
    for (EstadoOrden estado : EstadoOrden.estadosFinales()) {
      orden.setEstado(estado);
      assertThrows(IllegalStateException.class, () -> servicio.reasignarOrden(1L, 2L));
      assertFalse(orden.estaActiva());
    }
    verify(ordenes, never()).modificarOrden(any());
  }
}
