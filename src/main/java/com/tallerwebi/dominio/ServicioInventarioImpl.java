package com.tallerwebi.dominio;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioInventarioImpl implements ServicioInventario {

  private final RepositorioRepuesto repositorioRepuesto;

  @Autowired
  public ServicioInventarioImpl(RepositorioRepuesto repositorioRepuesto) {
    this.repositorioRepuesto = repositorioRepuesto;
  }

  @Override
  public List<Repuesto> listarTodos() {
    return repositorioRepuesto.obtenerTodos();
  }

  @Override
  public Repuesto buscarPorId(Long id) {
    return repositorioRepuesto.buscarPorId(id);
  }

  @Override
  public void guardarOActualizar(Repuesto repuesto) {
    if (repuesto.getPrecio() == null || repuesto.getPrecio() < 0) {
      throw new IllegalArgumentException("El precio no puede ser negativo");
    }
    if (repuesto.getStock() == null || repuesto.getStock() < 0) {
      throw new IllegalArgumentException("El stock no puede ser negativo");
    }
    repositorioRepuesto.guardar(repuesto);
  }

  @Override
  public void eliminar(Long id) {
    Repuesto repuesto = repositorioRepuesto.buscarPorId(id);
    if (repuesto != null) {
      repositorioRepuesto.eliminar(repuesto);
    }
  }
}
