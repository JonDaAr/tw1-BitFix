package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioCotizacionPreviaImpl implements ServicioCotizacionPrevia {

  private final RepositorioOrdenReparacion repositorioOrdenReparacion;
  private final ServicioHistorialOrden servicioHistorialOrden;

  @Autowired
  public ServicioCotizacionPreviaImpl(
    RepositorioOrdenReparacion repositorioOrdenReparacion,
    ServicioHistorialOrden servicioHistorialOrden
  ) {
    this.repositorioOrdenReparacion = repositorioOrdenReparacion;
    this.servicioHistorialOrden = servicioHistorialOrden;
  }

  @Override
  public void enviarCotizacionPrevia(Long idOrden, Double montoEstimado, String notaTecnica) {
    if (notaTecnica == null) {
      throw new IllegalArgumentException("Ingresá las observaciones de la cotización.");
    }
    String observaciones = notaTecnica.trim();
    OrdenReparacion orden = repositorioOrdenReparacion.buscarPorId(idOrden);
    if (orden.getEstado() != EstadoOrden.PENDIENTE_COTIZACION) {
      throw new IllegalStateException(
        "La cotización previa no se puede enviar en el estado actual: " + orden.getEstado()
      );
    }

    orden.setMontoTotal(montoEstimado);
    orden.setNotaTecnica(observaciones);
    orden.setEstado(EstadoOrden.COTIZACION_PREVIA_ENVIADA);

    servicioHistorialOrden.registrarEvento(orden, EstadoOrden.COTIZACION_PREVIA_ENVIADA);
    repositorioOrdenReparacion.modificarOrden(orden);
  }

  @Override
  public void aceptarCotizacionPrevia(Integer codigoSeguimiento, String emailUsuario) {
    responderCotizacionPrevia(codigoSeguimiento, emailUsuario, EstadoOrden.ESPERANDO_INGRESO);
  }

  @Override
  public void rechazarCotizacionPrevia(Integer codigoSeguimiento, String emailUsuario) {
    responderCotizacionPrevia(codigoSeguimiento, emailUsuario, EstadoOrden.COTIZACION_DESCARTADA);
  }

  private void responderCotizacionPrevia(
    Integer codigoSeguimiento,
    String emailUsuario,
    EstadoOrden estadoFinal
  ) {
    OrdenReparacion orden = repositorioOrdenReparacion.buscarPorCodigo(codigoSeguimiento);

    if (emailUsuario == null || !emailUsuario.equals(orden.getEmailCliente())) {
      throw new SecurityException("Acceso denegado.");
    }

    if (orden.getEstado() != EstadoOrden.COTIZACION_PREVIA_ENVIADA) {
      throw new IllegalStateException(
        "La cotización previa no se puede responder en el estado actual: " + orden.getEstado()
      );
    }

    orden.setEstado(estadoFinal);
    servicioHistorialOrden.registrarEvento(orden, estadoFinal);
    repositorioOrdenReparacion.modificarOrden(orden);
  }
}
