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

public class ServicioAsignacionTecnicoTest {

  private ServicioAsignacionTecnico servicioAsignacionTecnico;
  private RepositorioUsuario repositorioUsuarioMock;
  private RepositorioOrdenReparacion repositorioOrdenReparacionMock;

  @BeforeEach
  public void init() {
    repositorioUsuarioMock = mock(RepositorioUsuario.class);
    repositorioOrdenReparacionMock = mock(RepositorioOrdenReparacion.class);

    // Inyectamos ambos repositorios al servicio
    servicioAsignacionTecnico = new ServicioAsignacionTecnicoImpl(
            repositorioUsuarioMock,
            repositorioOrdenReparacionMock
    );
  }

  @Test
  public void deberiaAsignarTecnicoQueCoincidaConElNivelDeSoporteYTengaMenosCarga() throws NoHayTecnicosDisponibles {
    Usuario tecnicoNivel3Ocupado = crearTecnico(1L, "ocupado@test.com", NivelReparacion.NIVEL_3);
    Usuario tecnicoNivel3Libre = crearTecnico(2L, "libre@test.com", NivelReparacion.NIVEL_3);

    OrdenReparacion orden = new OrdenReparacion();
    orden.setNivelReparacion(NivelReparacion.NIVEL_3);

    when(repositorioUsuarioMock.buscarTecnicosActivos()).thenReturn(List.of(tecnicoNivel3Ocupado, tecnicoNivel3Libre));

    // Simulamos la carga de trabajo
    when(repositorioOrdenReparacionMock.contarOrdenesActivas(1L)).thenReturn(5L); // 5 órdenes
    when(repositorioOrdenReparacionMock.contarOrdenesActivas(2L)).thenReturn(1L); // 1 orden (Menos carga)

    servicioAsignacionTecnico.asignarTecnicoAOrden(orden);

    // Debe elegir al que tiene 1 sola orden
    assertThat(orden.getTecnicoAsignado(), notNullValue());
    assertThat(orden.getTecnicoAsignado(), equalTo(tecnicoNivel3Libre));
  }

  @Test
  public void deberiaDesempatarPorFechaDeUltimaAsignacionMasAntigua() throws NoHayTecnicosDisponibles {
    Usuario tecnicoUno = crearTecnico(1L, "tecnico1@test.com", NivelReparacion.NIVEL_3);
    Usuario tecnicoDos = crearTecnico(2L, "tecnico2@test.com", NivelReparacion.NIVEL_3);

    OrdenReparacion orden = new OrdenReparacion();
    orden.setNivelReparacion(NivelReparacion.NIVEL_3);

    when(repositorioUsuarioMock.buscarTecnicosActivos()).thenReturn(List.of(tecnicoUno, tecnicoDos));

    // Tienen la misma carga de trabajo (empate)
    when(repositorioOrdenReparacionMock.contarOrdenesActivas(1L)).thenReturn(2L);
    when(repositorioOrdenReparacionMock.contarOrdenesActivas(2L)).thenReturn(2L);

    // Fechas de asignación: el técnico 2 hace más tiempo que no recibe una orden
    when(repositorioOrdenReparacionMock.buscarFechaUltimaAsignacion(1L)).thenReturn(LocalDateTime.now().minusDays(1)); // Ayer
    when(repositorioOrdenReparacionMock.buscarFechaUltimaAsignacion(2L)).thenReturn(LocalDateTime.now().minusDays(5)); // Hace 5 días

    servicioAsignacionTecnico.asignarTecnicoAOrden(orden);

    // Debe elegir al técnico 2 porque hace más tiempo está libre
    assertThat(orden.getTecnicoAsignado(), equalTo(tecnicoDos));
  }

  @Test
  public void deberiaLanzarExcepcionSiNoHayTecnicosDelNivelRequerido() {
    Usuario tecnicoNivel1 = crearTecnico(1L, "tecnico1@test.com", NivelReparacion.NIVEL_1);
    OrdenReparacion orden = new OrdenReparacion();
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