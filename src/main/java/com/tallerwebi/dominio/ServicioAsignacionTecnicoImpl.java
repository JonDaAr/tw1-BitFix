package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioAsignacionTecnicoImpl implements ServicioAsignacionTecnico {

  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioOrdenReparacion repositorioOrdenReparacion;

  @Autowired
  public ServicioAsignacionTecnicoImpl(
          RepositorioUsuario repositorioUsuario,
          RepositorioOrdenReparacion repositorioOrdenReparacion
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioOrdenReparacion = repositorioOrdenReparacion;
  }

  @Override
  public void asignarTecnicoAOrden(OrdenReparacion orden) throws NoHayTecnicosDisponibles {
    List<Usuario> tecnicosActivos = this.repositorioUsuario.buscarTecnicosActivos();

    // 1. Filtramos los técnicos que coincidan con el nivel de soporte requerido
    List<Usuario> tecnicosValidos = filtrarTecnicosPorNivel(tecnicosActivos, orden.getNivelReparacion());

    if (tecnicosValidos.isEmpty()) {
      throw new NoHayTecnicosDisponibles("No hay técnicos disponibles con el nivel requerido para esta orden.");
    }

    // 2. Seleccionamos al mejor candidato (menor cantidad de órdenes activas y desempate por antigüedad)
    Usuario tecnicoSeleccionado = seleccionarTecnicoConMenorCarga(tecnicosValidos);

    // 3. Asignamos el técnico y la fecha a la orden
    orden.setTecnicoAsignado(tecnicoSeleccionado);
    orden.setFechaAsignacion(LocalDateTime.now());
  }

  private List<Usuario> filtrarTecnicosPorNivel(List<Usuario> tecnicos, NivelReparacion nivel) {
    return tecnicos.stream()
            .filter(t -> t.getNivelSoporte() != null && t.getNivelSoporte().equals(nivel))
            .collect(Collectors.toList());
  }

  private Usuario seleccionarTecnicoConMenorCarga(List<Usuario> tecnicos) {
    return tecnicos.stream()
            .min(Comparator
                    // Criterio 1: Menor cantidad de órdenes activas
                    .comparing((Usuario t) -> this.repositorioOrdenReparacion.contarOrdenesActivas(t.getId()))
                    // Criterio 2 (Desempate): La última asignación más antigua (o null si nunca se le asignó)
                    .thenComparing(t -> {
                      LocalDateTime ultimaAsignacion = this.repositorioOrdenReparacion.buscarFechaUltimaAsignacion(t.getId());
                      return ultimaAsignacion != null ? ultimaAsignacion : LocalDateTime.MIN;
                    }))
            .orElse(null);
  }
}