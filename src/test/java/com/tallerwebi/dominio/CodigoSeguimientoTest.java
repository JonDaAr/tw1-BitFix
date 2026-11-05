package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CodigoSeguimientoTest {

  private RepositorioOrdenReparacion repositorio;
  private ServicioOrdenReparacionImpl servicio;
  private OrdenReparacion orden;

  @BeforeEach
  public void preparar() {
    repositorio = mock(RepositorioOrdenReparacion.class);
    RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
    Usuario tecnico = new Usuario();
    tecnico.setNivelSoporte(NivelReparacion.NIVEL_3);
    when(usuarios.buscarTecnicosActivos()).thenReturn(List.of(tecnico));
    servicio = new ServicioOrdenReparacionImpl(usuarios, repositorio);
    orden = spy(new OrdenReparacion("Cliente", "123", "PC", "No enciende"));
    orden.setEmailCliente("cliente@test.com");
  }

  @Test
  public void reintentaSiElCodigoGeneradoYaExiste() throws Exception {
    doReturn(111111, 222222).when(orden).generarCodigoSeguimientoUnico();
    when(repositorio.existeCodigo(111111)).thenReturn(true);
    servicio.registrarOrden(orden);
    assertEquals(222222, orden.getCodigoSeguimiento());
    verify(repositorio).guardarOrden(orden);
  }

  @Test
  public void conservaUnCodigoDisponible() throws Exception {
    orden.setCodigoSeguimiento(333333);
    servicio.registrarOrden(orden);
    assertEquals(333333, orden.getCodigoSeguimiento());
    verify(orden, never()).generarCodigoSeguimientoUnico();
  }

  @Test
  public void agotamientoDeIntentosNoGuardaLaOrden() {
    doReturn(111111).when(orden).generarCodigoSeguimientoUnico();
    when(repositorio.existeCodigo(anyInt())).thenReturn(true);
    assertThrows(IllegalStateException.class, () -> servicio.registrarOrden(orden));
    verify(repositorio, never()).guardarOrden(any());
  }
}
