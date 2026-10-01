package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EstadoInvalidoParaCierreException;
import com.tallerwebi.dominio.excepcion.OrdenNoEncontradaException;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioCierreOrden")
@Transactional
public class ServicioCierreOrdenImpl implements ServicioCierreOrden {

  private static final EstadoOrden ESTADO_ENTREGADO = EstadoOrden.ENTREGADO;
  private static final EstadoOrden ESTADO_REPARADO = EstadoOrden.REPARADO;

  private RepositorioOrdenReparacion repositorioOrdenReparacion;

  @Autowired
  public ServicioCierreOrdenImpl(RepositorioOrdenReparacion repositorioOrdenReparacion) {
    this.repositorioOrdenReparacion = repositorioOrdenReparacion;
  }

  @Override
  public OrdenReparacion cerrarOrden(Long idOrden)
    throws OrdenNoEncontradaException, EstadoInvalidoParaCierreException {
    OrdenReparacion orden = repositorioOrdenReparacion.buscarPorId(idOrden);

    validarOrdenExistente(orden);
    validarEstado(orden.getEstado());
    validarMonto(orden.getMontoTotal());

    orden.setEstado(ESTADO_ENTREGADO);
    orden.setFechaEntrega(LocalDateTime.now());

    repositorioOrdenReparacion.modificarOrden(orden);
    return orden;
  }

  private void validarOrdenExistente(OrdenReparacion orden) throws OrdenNoEncontradaException {
    if (orden == null) {
      throw new OrdenNoEncontradaException("La orden buscada no existe.");
    }
  }

  private void validarEstado(EstadoOrden estado) throws EstadoInvalidoParaCierreException {
    if (estado == null) {
      throw new EstadoInvalidoParaCierreException("El estado de la orden no puede ser nulo.");
    }
    if (ESTADO_ENTREGADO == estado) {
      throw new EstadoInvalidoParaCierreException("La orden ya fue entregada anteriormente.");
    }
    if (ESTADO_REPARADO != estado) {
      throw new EstadoInvalidoParaCierreException(
        "Solo se pueden cerrar órdenes en estado REPARADO."
      );
    }
  }

  private void validarMonto(Double montoTotal) throws EstadoInvalidoParaCierreException {
    if (montoTotal == null || montoTotal <= 0) {
      throw new EstadoInvalidoParaCierreException(
        "No se puede cerrar una orden sin un presupuesto cargado válido."
      );
    }
  }

  @Override
  public OrdenReparacion obtenerOrdenParaComprobante(Long idOrden)
    throws OrdenNoEncontradaException {
    OrdenReparacion orden = repositorioOrdenReparacion.buscarPorId(idOrden);
    validarOrdenExistente(orden);
    return orden;
  }

  @Override
  public OrdenReparacion crearOrdenPruebaReparada() {
    OrdenReparacion ordenPrueba = new OrdenReparacion(
      "Juan Pérez",
      "1122334455",
      "Notebook Dell",
      "No enciende"
    );
    ordenPrueba.generarCodigoSeguimientoUnico();
    ordenPrueba.setEstado(ESTADO_REPARADO);
    ordenPrueba.setMontoTotal(25000.00);
    ordenPrueba.setNotaTecnica("Se reemplazó la fuente de alimentación.");

    repositorioOrdenReparacion.guardarOrden(ordenPrueba);
    return ordenPrueba;
  }
}
