package com.tallerwebi.dominio;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioHistorialOrdenImpl implements ServicioHistorialOrden {

  private final RepositorioHistorialOrden repositorioHistorialOrden;

  @Autowired
  public ServicioHistorialOrdenImpl(RepositorioHistorialOrden repositorioHistorialOrden) {
    this.repositorioHistorialOrden = repositorioHistorialOrden;
  }

  @Override
  public void registrarEvento(OrdenReparacion orden, EstadoOrden estado) {
    repositorioHistorialOrden.guardar(new HistorialOrden(orden, estado));
  }

  @Override
  public List<HistorialOrden> obtenerHistorial(Long ordenId) {
    return repositorioHistorialOrden.buscarPorOrden(ordenId);
  }
}
