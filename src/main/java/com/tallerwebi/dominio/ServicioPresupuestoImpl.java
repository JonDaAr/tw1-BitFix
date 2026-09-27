package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.SinStockException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioPresupuesto")
public class ServicioPresupuestoImpl implements ServicioPresupuesto {

  private final RepositorioRepuesto repositorioRepuesto;

  @Autowired
  public ServicioPresupuestoImpl(RepositorioRepuesto repositorioRepuesto) {
    this.repositorioRepuesto = repositorioRepuesto;
  }

  @Override
  public List<Repuesto> obtenerRepuestosDisponibles() {
    return this.repositorioRepuesto.obtenerDisponibles();
  }

  @Override
  public Double calcularSubtotalRepuesto(Long idRepuesto, Integer cantidad)
    throws SinStockException {
    Repuesto repuesto = this.repositorioRepuesto.buscarPorId(idRepuesto);
    if (repuesto == null || !repuesto.tieneStockSuficiente(cantidad)) {
      throw new SinStockException("No hay stock suficiente para el repuesto solicitado.");
    }
    return repuesto.getPrecio() * cantidad;
  }

  @Override
  public void descontarStockRepuesto(Long idRepuesto, Integer cantidad) throws SinStockException {
    Repuesto repuesto = this.repositorioRepuesto.buscarPorId(idRepuesto);
    if (repuesto == null || !repuesto.tieneStockSuficiente(cantidad)) {
      throw new SinStockException("No se puede descontar: stock insuficiente.");
    }
    repuesto.descontarStock(cantidad);
    this.repositorioRepuesto.guardar(repuesto);
  }
}
