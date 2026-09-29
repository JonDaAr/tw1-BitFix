package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioOrdenTest {

  private ServicioOrdenReparacion servicioOrden;

  private RepositorioUsuario repositorioUsuarioMock;

  private RepositorioOrdenReparacion repositorioOrdenMock;

  @BeforeEach
  public void init() {
    repositorioUsuarioMock = mock(RepositorioUsuario.class);

    repositorioOrdenMock = mock(RepositorioOrdenReparacion.class);

    servicioOrden = new ServicioOrdenReparacionImpl(repositorioUsuarioMock, repositorioOrdenMock);
  }

  @Test
  public void deberiaAsignarLaOrdenAlTecnicoConMenorCantidadDeOrdenesActivas() {
    Usuario tecnicoUno = crearTecnico(1L, "tecnico1@test.com");

    Usuario tecnicoDos = crearTecnico(2L, "tecnico2@test.com");

    OrdenReparacion OrdenReparacion = new OrdenReparacion();

    when(repositorioUsuarioMock.buscarTecnicosActivos())
      .thenReturn(List.of(tecnicoUno, tecnicoDos));

    when(repositorioOrdenMock.contarOrdenesActivas(1L)).thenReturn(3L);

    when(repositorioOrdenMock.contarOrdenesActivas(2L)).thenReturn(1L);

    when(repositorioOrdenMock.buscarFechaUltimaAsignacion(1L))
      .thenReturn(LocalDateTime.now().minusDays(1));

    when(repositorioOrdenMock.buscarFechaUltimaAsignacion(2L))
      .thenReturn(LocalDateTime.now().minusHours(1));

    OrdenReparacion resultado = servicioOrden.registrarOrden(OrdenReparacion);

    assertThat(resultado.getTecnicoAsignado(), equalTo(tecnicoDos));

    assertThat(resultado.getFechaAsignacion(), notNullValue());

    verify(repositorioOrdenMock).guardarOrden(OrdenReparacion);
  }

  @Test
  public void deberiaUsarLaUltimaAsignacionMasAntiguaCuandoHayEmpate() {
    Usuario tecnicoUno = crearTecnico(1L, "tecnico1@test.com");

    Usuario tecnicoDos = crearTecnico(2L, "tecnico2@test.com");

    OrdenReparacion OrdenReparacion = new OrdenReparacion();

    LocalDateTime asignacionUno = LocalDateTime.now().minusDays(5);

    LocalDateTime asignacionDos = LocalDateTime.now().minusDays(2);

    when(repositorioUsuarioMock.buscarTecnicosActivos())
      .thenReturn(List.of(tecnicoUno, tecnicoDos));

    when(repositorioOrdenMock.contarOrdenesActivas(1L)).thenReturn(2L);

    when(repositorioOrdenMock.contarOrdenesActivas(2L)).thenReturn(2L);

    when(repositorioOrdenMock.buscarFechaUltimaAsignacion(1L)).thenReturn(asignacionUno);

    when(repositorioOrdenMock.buscarFechaUltimaAsignacion(2L)).thenReturn(asignacionDos);

    OrdenReparacion resultado = servicioOrden.registrarOrden(OrdenReparacion);

    assertThat(resultado.getTecnicoAsignado(), equalTo(tecnicoUno));
  }

  @Test
  public void tecnicoNuncaAsignadoDeberiaGanarElDesempateFrenteAUnoYaAsignado() {
    Usuario tecnicoUno = crearTecnico(1L, "tecnico1@test.com");

    Usuario tecnicoDos = crearTecnico(2L, "tecnico2@test.com");

    OrdenReparacion OrdenReparacion = new OrdenReparacion();

    when(repositorioUsuarioMock.buscarTecnicosActivos())
      .thenReturn(List.of(tecnicoUno, tecnicoDos));

    when(repositorioOrdenMock.contarOrdenesActivas(1L)).thenReturn(1L);

    when(repositorioOrdenMock.contarOrdenesActivas(2L)).thenReturn(1L);

    when(repositorioOrdenMock.buscarFechaUltimaAsignacion(1L)).thenReturn(LocalDateTime.now());

    when(repositorioOrdenMock.buscarFechaUltimaAsignacion(2L)).thenReturn(null);

    OrdenReparacion resultado = servicioOrden.registrarOrden(OrdenReparacion);

    assertThat(resultado.getTecnicoAsignado(), equalTo(tecnicoDos));
  }

  @Test
  public void noDeberiaRegistrarLaOrdenSiNoHayTecnicosActivos() {
    OrdenReparacion OrdenReparacion = new OrdenReparacion();

    when(repositorioUsuarioMock.buscarTecnicosActivos()).thenReturn(List.of());

    assertThrows(
      NoHayTecnicosDisponibles.class,
      () -> servicioOrden.registrarOrden(OrdenReparacion)
    );

    verify(repositorioOrdenMock, never()).guardarOrden(any(OrdenReparacion.class));
  }

  private Usuario crearTecnico(Long id, String email) {
    Usuario tecnico = new Usuario();

    tecnico.setId(id);
    tecnico.setEmail(email);
    tecnico.setRol("TECNICO");
    tecnico.setActivo(true);

    return tecnico;
  }
}
