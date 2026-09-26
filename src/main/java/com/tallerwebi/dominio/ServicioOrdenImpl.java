package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioOrden")
@Transactional
public class ServicioOrdenImpl implements ServicioOrden {

  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioOrden repositorioOrden;

  @Autowired
  public ServicioOrdenImpl(
    RepositorioUsuario repositorioUsuario,
    RepositorioOrden repositorioOrden
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioOrden = repositorioOrden;
  }

  @Override
  public Orden registrarOrden(Orden orden) {
    List<Usuario> tecnicos = repositorioUsuario.buscarTecnicosActivos();

    if (tecnicos.isEmpty()) {
      throw new NoHayTecnicosDisponibles();
    }

    Usuario tecnicoSeleccionado = seleccionarTecnico(tecnicos);

    orden.setTecnicoAsignado(tecnicoSeleccionado);
    orden.setFechaAsignacion(LocalDateTime.now());

    repositorioOrden.guardar(orden);

    return orden;
  }

  private Usuario seleccionarTecnico(List<Usuario> tecnicos) {
    Usuario seleccionado = tecnicos.get(0);

    long menorCantidad = repositorioOrden.contarOrdenesActivas(seleccionado.getId());

    LocalDateTime ultimaAsignacion = repositorioOrden.buscarFechaUltimaAsignacion(
      seleccionado.getId()
    );

    for (int i = 1; i < tecnicos.size(); i++) {
      Usuario candidato = tecnicos.get(i);

      long cantidad = repositorioOrden.contarOrdenesActivas(candidato.getId());

      LocalDateTime ultimaAsignacionCandidato = repositorioOrden.buscarFechaUltimaAsignacion(
        candidato.getId()
      );

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
