package com.tallerwebi.dominio;

final class ServicioResolucionCanje {

  private final RepositorioCanje repositorio;

  ServicioResolucionCanje(RepositorioCanje repositorio) {
    this.repositorio = repositorio;
  }

  void resolverSolicitud(Long solicitudId, boolean aprobar, Double valorAprobado) {
    SolicitudCanje solicitud = buscarSolicitudPendiente(solicitudId);
    if (aprobar) {
      aprobarSolicitud(solicitud, valorAprobado);
    } else {
      solicitud.setEstado(EstadoSolicitudCanje.RECHAZADA);
    }
  }

  void responderSolicitudAdmin(
    Long solicitudId,
    boolean contraofertar,
    boolean aprobar,
    Double valor,
    String comentario
  ) {
    SolicitudCanje solicitud = buscarSolicitudPendiente(solicitudId);
    String comentarioNormalizado = comentario == null ? null : comentario.trim();
    if (comentarioNormalizado != null && comentarioNormalizado.length() > 1000) {
      throw new IllegalArgumentException("El comentario no puede superar los 1000 caracteres.");
    }
    if (contraofertar) {
      validarValorAprobado(solicitud, valor);
      solicitud.setValorAprobado(valor);
      solicitud.setComentarioRespuesta(comentarioNormalizado);
      solicitud.setEstado(EstadoSolicitudCanje.CONTRAOFERTA);
      return;
    }
    if (aprobar) {
      aprobarSolicitud(solicitud, valor);
    } else {
      solicitud.setEstado(EstadoSolicitudCanje.RECHAZADA);
    }
    solicitud.setComentarioRespuesta(comentarioNormalizado);
  }

  void responderContraoferta(Long solicitudId, Long clienteId, boolean aceptar) {
    SolicitudCanje solicitud = repositorio.buscarSolicitud(solicitudId);
    if (
      solicitud == null ||
      solicitud.getCliente() == null ||
      !clienteId.equals(solicitud.getCliente().getId())
    ) {
      throw new IllegalArgumentException("La solicitud no existe.");
    }
    if (solicitud.getEstado() != EstadoSolicitudCanje.CONTRAOFERTA) {
      throw new IllegalArgumentException("La solicitud no tiene una contraoferta pendiente.");
    }
    if (!aceptar) {
      solicitud.setEstado(EstadoSolicitudCanje.RECHAZADA);
      return;
    }
    ComputadoraUsada computadora = repositorio.buscarComputadoraConBloqueo(
      solicitud.getComputadora().getId()
    );
    validarStockAprobacion(computadora);
    computadora.setStock(computadora.getStock() - 1);
    solicitud.setEstado(EstadoSolicitudCanje.APROBADA);
    repositorio.guardarComputadora(computadora);
  }

  private SolicitudCanje buscarSolicitudPendiente(Long solicitudId) {
    SolicitudCanje solicitud = repositorio.buscarSolicitud(solicitudId);
    if (solicitud == null) {
      throw new IllegalArgumentException("La solicitud no existe.");
    }
    if (solicitud.getEstado() != EstadoSolicitudCanje.PENDIENTE) {
      throw new IllegalArgumentException("La solicitud ya fue resuelta.");
    }
    return solicitud;
  }

  private void aprobarSolicitud(SolicitudCanje solicitud, Double valorAprobado) {
    validarValorAprobado(solicitud, valorAprobado);
    ComputadoraUsada computadora = repositorio.buscarComputadoraConBloqueo(
      solicitud.getComputadora().getId()
    );
    validarStockAprobacion(computadora);
    computadora.setStock(computadora.getStock() - 1);
    solicitud.setValorAprobado(valorAprobado);
    solicitud.setEstado(EstadoSolicitudCanje.APROBADA);
    repositorio.guardarComputadora(computadora);
  }

  private void validarValorAprobado(SolicitudCanje solicitud, Double valorAprobado) {
    if (valorAprobado == null) {
      throw new IllegalArgumentException("Indicá la tasación final.");
    }
    if (valorAprobado < 0) {
      throw new IllegalArgumentException("La tasación no puede ser negativa.");
    }
    if (valorAprobado > solicitud.getComputadora().getPrecio()) {
      throw new IllegalArgumentException(
        "La tasación no puede superar el precio de la computadora."
      );
    }
  }

  private void validarStockAprobacion(ComputadoraUsada computadora) {
    if (computadora == null || !Boolean.TRUE.equals(computadora.getActiva())) {
      throw new IllegalStateException("La computadora ya no está disponible.");
    }
    if (computadora.getStock() == null || computadora.getStock() < 1) {
      throw new IllegalStateException("La computadora ya no tiene unidades disponibles.");
    }
  }
}
