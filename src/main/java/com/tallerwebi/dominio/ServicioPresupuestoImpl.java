package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.SinStockException;
import com.tallerwebi.presentacion.ItemPresupuestoForm;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioPresupuestoImpl implements ServicioPresupuesto {

  private final RepositorioRepuesto repositorioRepuesto;
  private final RepositorioOrdenReparacion repositorioOrden;
  private static final double PISO_MANO_OBRA = 20000.0;
  private static final double TOPE_MANO_OBRA = 60000.0;
  private static final double LIMITE_PISO = 50000.0;
  private static final double LIMITE_TRAMO_MEDIO = 150000.0;
  private static final double PORCENTAJE_MEDIO = 0.35;
  private static final double PORCENTAJE_ALTO = 0.25;

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
    return repositorioRepuesto.obtenerDisponibles();
  }

  @Override
  public Double calcularSubtotal(Long repuestoId, Integer cantidad) throws SinStockException {
    Repuesto repuesto = buscarRepuestoConStock(repuestoId, cantidad);
    return repuesto.getPrecio() * cantidad;
  }

  private Repuesto buscarRepuestoConStock(Long repuestoId, Integer cantidad) {
    validarCantidad(cantidad);
    Repuesto repuesto = repositorioRepuesto.buscarPorId(repuestoId);
    if (repuesto == null) {
      throw new IllegalArgumentException("El repuesto solicitado no existe.");
    }
    if (repuesto.getStock() < cantidad) {
      throw new SinStockException("Stock insuficiente para: " + repuesto.getNombre());
    }
    return repuesto;
  }

  @Override
  public Double calcularSubtotalRepuesto(Long repuestoId, Integer cantidad)
    throws SinStockException {
    return calcularSubtotal(repuestoId, cantidad);
  }

  @Override
  public void descontarStockRepuesto(Long repuestoId, Integer cantidad) throws SinStockException {
    Repuesto repuesto = buscarRepuestoConStock(repuestoId, cantidad);
    repuesto.setStock(repuesto.getStock() - cantidad);
    this.repositorioRepuesto.guardar(repuesto);
  }

  @Override
  public Double calcularTotalPresupuesto(List<ItemPresupuestoForm> items) throws SinStockException {
    double subtotalRepuestos = calcularSubtotalRepuestos(items);
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

    if (orden.getEstado() == null || !Set.of(EstadoOrden.RECIBIDO, EstadoOrden.EN_DIAGNOSTICO, EstadoOrden.ESPERANDO_REPUESTO)
      .contains(orden.getEstado())) {
      throw new IllegalStateException("No se puede enviar un presupuesto en el estado actual.");
    }
    if (!orden.getReservasPresupuesto().isEmpty()) {
      throw new IllegalStateException("La orden ya tiene un presupuesto reservado.");
    }
    if (total == null || !Double.isFinite(total) || total <= 0) {
      throw new IllegalArgumentException("El presupuesto debe tener un monto positivo y finito.");
    }
    List<ReservaRepuesto> reservas = prepararReservas(items);
    for (ReservaRepuesto reserva : reservas) {
      Repuesto repuesto = reserva.getRepuesto();
      repuesto.descontarStock(reserva.getCantidad());
      repositorioRepuesto.guardar(repuesto);
    }
    orden.getReservasPresupuesto().addAll(reservas);

    orden.setMontoTotal(total);
    orden.setEstado(EstadoOrden.PRESUPUESTO_ENVIADO);

    repositorioOrden.modificarOrden(orden);
  }

  private void validarCantidad(Integer cantidad) {
    if (cantidad == null || cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
    }
  }

  private List<ReservaRepuesto> prepararReservas(List<ItemPresupuestoForm> items) {
    Map<Long, Integer> cantidades = new LinkedHashMap<>();
    if (items != null) {
      for (ItemPresupuestoForm item : items) {
        if (esItemValido(item)) {
          cantidades.merge(item.getRepuestoId(), item.getCantidad(), this::sumarCantidades);
        }
      }
    }
    List<ReservaRepuesto> reservas = new ArrayList<>();
    for (Map.Entry<Long, Integer> item : cantidades.entrySet()) {
      Repuesto repuesto = buscarRepuestoConStock(item.getKey(), item.getValue());
      reservas.add(new ReservaRepuesto(repuesto, item.getValue()));
    }
    return reservas;
  }

  private Integer sumarCantidades(Integer primera, Integer segunda) {
    try {
      return Math.addExact(primera, segunda);
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException("La cantidad solicitada es demasiado grande.", e);
    }
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
    double subtotal = 0.0;
    for (ReservaRepuesto reserva : prepararReservas(items)) {
      subtotal += reserva.getRepuesto().getPrecio() * reserva.getCantidad();
    }
    return subtotal;
  }
}
