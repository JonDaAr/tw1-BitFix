package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.SinStockException;
import com.tallerwebi.presentacion.ItemPresupuestoForm;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioPresupuestoImpl implements ServicioPresupuesto {

  private final RepositorioRepuesto repositorioRepuesto;
  private final RepositorioOrdenReparacion repositorioOrden;

  @Autowired
  public ServicioPresupuestoImpl(
    RepositorioRepuesto repositorioRepuesto,
    RepositorioOrdenReparacion repositorioOrden
  ) {
    this.repositorioRepuesto = repositorioRepuesto;
    this.repositorioOrden = repositorioOrden;
  }

  @Override
  public List<Repuesto> obtenerRepuestosDisponibles() {
    return repositorioRepuesto.obtenerRepuestosDisponibles();
  }

  @Override
  public Double calcularSubtotal(Long repuestoId, Integer cantidad) throws SinStockException {
    Repuesto repuesto = repositorioRepuesto.buscarPorId(repuestoId);
    if (repuesto == null) {
      throw new IllegalArgumentException("El repuesto solicitado no existe.");
    }
    if (repuesto.getStock() < cantidad) {
      throw new SinStockException("Stock insuficiente para: " + repuesto.getNombre());
    }
    return repuesto.getPrecio() * cantidad;
  }

  @Override
  public Double calcularSubtotalRepuesto(Long repuestoId, Integer cantidad)
    throws SinStockException {
    return calcularSubtotal(repuestoId, cantidad);
  }

  @Override
  public void descontarStockRepuesto(Long repuestoId, Integer cantidad) throws SinStockException {
    Repuesto repuesto = repositorioRepuesto.buscarPorId(repuestoId);
    if (repuesto == null) {
      throw new IllegalArgumentException("El repuesto solicitado no existe.");
    }
    if (repuesto.getStock() < cantidad) {
      throw new SinStockException("Stock insuficiente para: " + repuesto.getNombre());
    }
    repuesto.setStock(repuesto.getStock() - cantidad);
    this.repositorioRepuesto.guardar(repuesto);
  }

  @Override
  public Double calcularTotalPresupuesto(List<ItemPresupuestoForm> items) throws SinStockException {
    if (items == null || items.isEmpty()) {
      return 0.0;
    }

    double total = 0.0;
    for (ItemPresupuestoForm item : items) {
      if (item.getRepuestoId() != null && item.getCantidad() != null && item.getCantidad() > 0) {
        total += calcularSubtotal(item.getRepuestoId(), item.getCantidad());
      }
    }
    return total;
  }

  @Override
  public void generarYEnviarPresupuesto(
    Integer codigoSeguimiento,
    Double costoManoDeObra,
    String diagnostico
  ) {
    OrdenReparacion orden = repositorioOrden.buscarPorCodigo(codigoSeguimiento);
    if (orden != null) {
      orden.setEstado(EstadoOrden.PRESUPUESTO_ENVIADO);
      orden.setMontoTotal(costoManoDeObra);
      orden.setNotaTecnica(diagnostico);

      repositorioOrden.modificarOrden(orden);
    }
  }

  @Override
  public void enviarPresupuesto(
    Integer codigoSeguimiento,
    List<ItemPresupuestoForm> items,
    Double total
  ) throws SinStockException {
    OrdenReparacion orden = buscarOrden(codigoSeguimiento);

    verificarStock(items);
    descontarStock(items);

    orden.setMontoTotal(total);
    orden.setEstado(EstadoOrden.PRESUPUESTO_ENVIADO);

    repositorioOrden.modificarOrden(orden);
  }

  private OrdenReparacion buscarOrden(Integer codigoSeguimiento) {
    OrdenReparacion orden = repositorioOrden.buscarPorCodigo(codigoSeguimiento);

    if (orden == null) {
      throw new IllegalArgumentException(
        "No se encontró la orden con el código de seguimiento: " + codigoSeguimiento
      );
    }

    return orden;
  }

  private void verificarStock(List<ItemPresupuestoForm> items) throws SinStockException {
    if (items == null) {
      return;
    }

    for (ItemPresupuestoForm item : items) {
      if (esItemValido(item)) {
        calcularSubtotal(item.getRepuestoId(), item.getCantidad());
      }
    }
  }

  private void descontarStock(List<ItemPresupuestoForm> items) throws SinStockException {
    if (items == null) {
      return;
    }

    for (ItemPresupuestoForm item : items) {
      if (esItemValido(item)) {
        descontarStockRepuesto(item.getRepuestoId(), item.getCantidad());
      }
    }
  }

  private boolean esItemValido(ItemPresupuestoForm item) {
    return (
      item != null &&
      item.getRepuestoId() != null &&
      item.getCantidad() != null &&
      item.getCantidad() > 0
    );
  }
}
