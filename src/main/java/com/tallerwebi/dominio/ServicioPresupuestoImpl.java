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
  private final RepositorioDetallePresupuesto repositorioDetallePresupuesto;
  private static final double PISO_MANO_OBRA = 20000.0;
  private static final double TOPE_MANO_OBRA = 60000.0;
  private static final double LIMITE_PISO = 50000.0;
  private static final double LIMITE_TRAMO_MEDIO = 150000.0;
  private static final double PORCENTAJE_MEDIO = 0.35;
  private static final double PORCENTAJE_ALTO = 0.25;

  @Autowired
  public ServicioPresupuestoImpl(
    RepositorioRepuesto repositorioRepuesto,
    RepositorioOrdenReparacion repositorioOrden,
    RepositorioDetallePresupuesto repositorioDetallePresupuesto
  ) {
    this.repositorioRepuesto = repositorioRepuesto;
    this.repositorioOrden = repositorioOrden;
    this.repositorioDetallePresupuesto = repositorioDetallePresupuesto;
  }

  @Override
  public List<Repuesto> obtenerRepuestosDisponibles() {
    return repositorioRepuesto.obtenerDisponibles();
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
    // CAMBIO CLAVE: Usamos el método con bloqueo pesimista
    Repuesto repuesto = repositorioRepuesto.buscarPorIdConBloqueo(repuestoId);

    if (repuesto == null) {
      throw new IllegalArgumentException("El repuesto solicitado no existe.");
    }
    // Al estar bloqueado, esta validación ahora es 100% segura frente a concurrencia
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

    double subtotalRepuestos = 0.0;

    for (ItemPresupuestoForm item : items) {
      if (item.getCantidad() != null && item.getCantidad() > 0) {
        subtotalRepuestos += calcularSubtotalRepuesto(item.getRepuestoId(), item.getCantidad());
      }
    }

    double manoDeObra = calcularManoDeObra(subtotalRepuestos);

    return subtotalRepuestos + manoDeObra;
  }

  @Override
  public void enviarPresupuesto(
    Integer codigoSeguimiento,
    List<ItemPresupuestoForm> items,
    Double total
  ) throws SinStockException {
    OrdenReparacion orden = buscarOrden(codigoSeguimiento);

    if (orden.getEstado() == EstadoOrden.PRESUPUESTO_ENVIADO) {
      throw new IllegalStateException("El presupuesto ya fue enviado anteriormente.");
    }

    verificarStock(items);
    descontarStock(items);
    guardarDetallesPresupuesto(orden, items);

    orden.setMontoTotal(total);
    orden.setEstado(EstadoOrden.PRESUPUESTO_ENVIADO);

    repositorioOrden.modificarOrden(orden);
  }

  @Override
  public void devolverStockPorOrden(Long ordenId) {
    if (ordenId == null) {
      throw new IllegalArgumentException("La orden es obligatoria para devolver el stock.");
    }

    List<DetallePresupuesto> detalles = repositorioDetallePresupuesto.buscarPorOrden(ordenId);
    for (DetallePresupuesto detalle : detalles) {
      devolverStock(detalle);
    }
  }

  private void devolverStock(DetallePresupuesto detalle) {
    if (detalle.isStockDevuelto()) {
      return;
    }

    Repuesto repuestoDetalle = detalle.getRepuesto();
    if (repuestoDetalle == null || detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
      throw new IllegalStateException("El detalle del presupuesto no contiene datos válidos.");
    }

    Repuesto repuesto = repositorioRepuesto.buscarPorIdConBloqueo(repuestoDetalle.getId());
    if (repuesto == null || repuesto.getStock() == null) {
      throw new IllegalStateException(
        "No se puede devolver el stock del repuesto asociado al presupuesto."
      );
    }

    repuesto.setStock(repuesto.getStock() + detalle.getCantidad());
    repositorioRepuesto.guardar(repuesto);

    detalle.setStockDevuelto(true);
    repositorioDetallePresupuesto.modificar(detalle);
  }

  @Override
  public Double calcularManoDeObra(Double subtotalRepuestos) {
    if (subtotalRepuestos == null || subtotalRepuestos <= 0.0) {
      return 0.0;
    }

    if (subtotalRepuestos <= LIMITE_PISO) {
      return PISO_MANO_OBRA;
    }

    if (subtotalRepuestos <= LIMITE_TRAMO_MEDIO) {
      return subtotalRepuestos * PORCENTAJE_MEDIO;
    }

    double calculada = subtotalRepuestos * PORCENTAJE_ALTO;
    return Math.min(calculada, TOPE_MANO_OBRA);
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

  private void guardarDetallesPresupuesto(OrdenReparacion orden, List<ItemPresupuestoForm> items) {
    if (items == null) {
      return;
    }

    for (ItemPresupuestoForm item : items) {
      if (esItemValido(item)) {
        Repuesto repuesto = repositorioRepuesto.buscarPorId(item.getRepuestoId());
        if (repuesto == null) {
          throw new IllegalArgumentException("El repuesto solicitado no existe.");
        }
        repositorioDetallePresupuesto.guardar(
          new DetallePresupuesto(orden, repuesto, item.getCantidad())
        );
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

  @Override
  public Double calcularSubtotalRepuestos(List<ItemPresupuestoForm> items)
    throws SinStockException {
    if (items == null || items.isEmpty()) {
      return 0.0;
    }
    double subtotal = 0.0;
    for (ItemPresupuestoForm item : items) {
      if (item.getRepuestoId() != null && item.getCantidad() != null && item.getCantidad() > 0) {
        subtotal += calcularSubtotal(item.getRepuestoId(), item.getCantidad());
      }
    }
    return subtotal;
  }
}
