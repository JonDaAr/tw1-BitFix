package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioAsignacionTecnicoImpl implements ServicioAsignacionTecnico {

  private final RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioAsignacionTecnicoImpl(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public void asignarTecnicoAOrden(OrdenReparacion orden) throws NoHayTecnicosDisponibles {
    List<Usuario> tecnicosActivos = this.repositorioUsuario.buscarTecnicosActivos();

    if (tecnicosActivos == null || tecnicosActivos.isEmpty()) {
      throw new NoHayTecnicosDisponibles();
    }

    Usuario tecnicoAsignado = tecnicosActivos
      .stream()
      .filter(tecnico -> orden.getNivelReparacion().equals(tecnico.getNivelSoporte()))
      .findFirst()
      .orElseThrow(NoHayTecnicosDisponibles::new);

    // (puede que lo tengas como setUsuario, setTecnicoAsignado, etc.)
    orden.setTecnicoAsignado(tecnicoAsignado);
  }
}
