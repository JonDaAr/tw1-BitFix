package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AsignadorTecnicos {

  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioOrdenReparacion repositorioOrdenReparacion;

  public AsignadorTecnicos(RepositorioUsuario usuarios, RepositorioOrdenReparacion ordenes) {
    this.repositorioUsuario = usuarios;
    this.repositorioOrdenReparacion = ordenes;
  }
  public void asignar(OrdenReparacion orden) {
    // 1. Traemos a todos los técnicos activos
    List<Usuario> tecnicos = repositorioUsuario.buscarTecnicosActivos();
    if (tecnicos.isEmpty()) {
      throw new NoHayTecnicosDisponibles();
    }

    // 2. Filtramos la lista para quedarnos SOLO con los que tengan el mismo nivel de la orden
    List<Usuario> tecnicosFiltrados = tecnicos
      .stream()
      .filter(tecnico -> tecnico.getNivelSoporte() == orden.getNivelReparacion())
      .collect(Collectors.toList());

    // 3. Si no hay ningún técnico de ese nivel específico, lanzamos la excepción
    if (tecnicosFiltrados.isEmpty()) {
      throw new NoHayTecnicosDisponibles();
    }

    // 4. Aplicamos tu algoritmo de menor cantidad de órdenes sobre la lista ya filtrada
    Usuario tecnicoSeleccionado = seleccionarTecnico(tecnicosFiltrados);
    orden.setTecnicoAsignado(tecnicoSeleccionado);
    orden.setFechaAsignacion(LocalDateTime.now());
  }

  private Usuario seleccionarTecnico(List<Usuario> tecnicos) {
    Usuario seleccionado = tecnicos.get(0);
    long menorCantidad = repositorioOrdenReparacion.contarOrdenesActivas(seleccionado.getId());
    LocalDateTime ultimaAsignacion = repositorioOrdenReparacion.buscarFechaUltimaAsignacion(
      seleccionado.getId()
    );

    for (int i = 1; i < tecnicos.size(); i++) {
      Usuario candidato = tecnicos.get(i);
      long cantidad = repositorioOrdenReparacion.contarOrdenesActivas(candidato.getId());
      LocalDateTime ultimaAsignacionCandidato =
        repositorioOrdenReparacion.buscarFechaUltimaAsignacion(candidato.getId());

      if (debeSeleccionarse(cantidad, ultimaAsignacionCandidato, menorCantidad, ultimaAsignacion)) {
        seleccionado = candidato;
        menorCantidad = cantidad;
        ultimaAsignacion = ultimaAsignacionCandidato;
      }
    }

    return seleccionado;
  }

  private boolean debeSeleccionarse(
    long cantidadCandidato,
    LocalDateTime ultimaAsignacionCandidato,
    long menorCantidad,
    LocalDateTime ultimaAsignacionSeleccionado
  ) {
    if (cantidadCandidato < menorCantidad) {
      return true;
    }
    if (cantidadCandidato > menorCantidad) {
      return false;
    }
    if (ultimaAsignacionSeleccionado == null) {
      return false;
    }
    return (
      ultimaAsignacionCandidato == null ||
      ultimaAsignacionCandidato.isBefore(ultimaAsignacionSeleccionado)
    );
  }

}
