package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioAsignacionTecnicoTest {

  private ServicioAsignacionTecnico servicioAsignacionTecnico;
  private RepositorioUsuario repositorioUsuarioMock;

  @BeforeEach
  public void init() {
    repositorioUsuarioMock = mock(RepositorioUsuario.class);
    // Instanciamos el nuevo servicio inyectándole el repositorio de usuarios
    servicioAsignacionTecnico = new ServicioAsignacionTecnicoImpl(repositorioUsuarioMock);
  }

  @Test
  public void deberiaAsignarTecnicoQueCoincidaConElNivelDeSoporte()
    throws NoHayTecnicosDisponibles {
    Usuario tecnicoNivel2 = crearTecnico(1L, "tecnico2@test.com", NivelReparacion.NIVEL_2);
    Usuario tecnicoNivel3 = crearTecnico(2L, "tecnico3@test.com", NivelReparacion.NIVEL_3);

    OrdenReparacion orden = new OrdenReparacion();
    orden.setNivelReparacion(NivelReparacion.NIVEL_3);

    when(repositorioUsuarioMock.buscarTecnicosActivos())
      .thenReturn(List.of(tecnicoNivel2, tecnicoNivel3));

    servicioAsignacionTecnico.asignarTecnicoAOrden(orden);

    // Verificamos que se haya asignado el técnico de Nivel 3, ignorando al de Nivel 2
    assertThat(orden.getTecnicoAsignado(), notNullValue());
    assertThat(orden.getTecnicoAsignado(), equalTo(tecnicoNivel3));
  }

  @Test
  public void deberiaLanzarExcepcionSiNoHayTecnicosDelNivelRequerido() {
    Usuario tecnicoNivel1 = crearTecnico(1L, "tecnico1@test.com", NivelReparacion.NIVEL_1);

    OrdenReparacion orden = new OrdenReparacion();
    // La orden pide nivel 3, pero solo hay nivel 1 disponible
    orden.setNivelReparacion(NivelReparacion.NIVEL_3);

    when(repositorioUsuarioMock.buscarTecnicosActivos()).thenReturn(List.of(tecnicoNivel1));

    assertThrows(
      NoHayTecnicosDisponibles.class,
      () -> servicioAsignacionTecnico.asignarTecnicoAOrden(orden)
    );
  }

  @Test
  public void deberiaLanzarExcepcionSiNoHayNingunTecnicoActivo() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setNivelReparacion(NivelReparacion.NIVEL_2);

    when(repositorioUsuarioMock.buscarTecnicosActivos()).thenReturn(List.of());

    assertThrows(
      NoHayTecnicosDisponibles.class,
      () -> servicioAsignacionTecnico.asignarTecnicoAOrden(orden)
    );
  }

  private Usuario crearTecnico(Long id, String email, NivelReparacion nivel) {
    Usuario tecnico = new Usuario();
    tecnico.setId(id);
    tecnico.setEmail(email);
    tecnico.setRol("TECNICO");
    tecnico.setActivo(true);
    tecnico.setNivelSoporte(nivel);
    return tecnico;
  }
}
