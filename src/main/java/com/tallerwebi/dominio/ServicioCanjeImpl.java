package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioCanjeImpl implements ServicioCanje {

  private final RepositorioCanje repositorioCanje;

  @Autowired
  public ServicioCanjeImpl(RepositorioCanje repositorioCanje) {
    this.repositorioCanje = repositorioCanje;
  }

  @Override
  public List<ComputadoraUsada> listarDisponibles() {
    return repositorioCanje.listarComputadorasDisponibles();
  }

  @Override
  public List<ComputadoraUsada> listarTodasLasComputadoras() {
    return repositorioCanje.listarComputadoras();
  }

  @Override
  public List<SolicitudCanje> listarSolicitudes() {
    return repositorioCanje.listarSolicitudes();
  }

  @Override
  public List<SolicitudCanje> listarSolicitudesDeCliente(Long clienteId) {
    return repositorioCanje.listarSolicitudesDeCliente(clienteId);
  }

  @Override
  public void guardarComputadora(ComputadoraUsada computadora) {
    validarComputadora(computadora);
    if (computadora.getActiva() == null) {
      computadora.setActiva(true);
    }
    repositorioCanje.guardarComputadora(computadora);
  }

  @Override
  public void pausarComputadora(Long computadoraId) {
    ComputadoraUsada computadora = repositorioCanje.buscarComputadora(computadoraId);
    if (computadora == null) {
      throw new IllegalArgumentException("La computadora no existe.");
    }
    computadora.setActiva(false);
  }

  @Override
  public void solicitarCanje(
    Long computadoraId,
    Usuario cliente,
    String marca,
    String modelo,
    String descripcion,
    Double valorOfrecido
  ) {
    validarCliente(cliente);
    validarDatosEquipo(marca, modelo, descripcion);
    validarValorOfrecido(valorOfrecido);
    ComputadoraUsada computadora = buscarComputadoraDisponible(computadoraId);

    SolicitudCanje solicitud = new SolicitudCanje();
    solicitud.setCliente(cliente);
    solicitud.setComputadora(computadora);
    solicitud.setEquipoMarca(marca.trim());
    solicitud.setEquipoModelo(modelo.trim());
    solicitud.setEquipoDescripcion(descripcion.trim());
    solicitud.setValorOfrecido(valorOfrecido);
    solicitud.setEstado(EstadoSolicitudCanje.PENDIENTE);
    repositorioCanje.guardarSolicitud(solicitud);
  }

  @Override
  public void resolverSolicitud(Long solicitudId, boolean aprobar, Double valorAprobado) {
    SolicitudCanje solicitud = buscarSolicitudPendiente(solicitudId);
    if (aprobar) {
      aprobarSolicitud(solicitud, valorAprobado);
    } else {
      solicitud.setEstado(EstadoSolicitudCanje.RECHAZADA);
    }
  }

  private void validarComputadora(ComputadoraUsada computadora) {
    if (computadora == null) {
      throw new IllegalArgumentException("Completá los datos de la computadora.");
    }
    validarMarca(computadora.getMarca());
    validarModelo(computadora.getModelo());
    validarCondicion(computadora.getCondicion());
    validarPrecio(computadora.getPrecio());
    validarStock(computadora.getStock());
  }

  private void validarMarca(String marca) {
    if (vacio(marca)) {
      throw new IllegalArgumentException("La marca es obligatoria.");
    }
  }

  private void validarModelo(String modelo) {
    if (vacio(modelo)) {
      throw new IllegalArgumentException("El modelo es obligatorio.");
    }
  }

  private void validarCondicion(String condicion) {
    if (vacio(condicion)) {
      throw new IllegalArgumentException("La condición es obligatoria.");
    }
  }

  private void validarPrecio(Double precio) {
    if (precio == null || precio <= 0) {
      throw new IllegalArgumentException("El precio debe ser mayor a cero.");
    }
  }

  private void validarStock(Integer stock) {
    if (stock == null || stock < 0) {
      throw new IllegalArgumentException("El stock no puede ser negativo.");
    }
  }

  private void validarCliente(Usuario cliente) {
    if (cliente == null || cliente.getId() == null) {
      throw new IllegalArgumentException("Se requiere un cliente registrado.");
    }
  }

  private void validarDatosEquipo(String marca, String modelo, String descripcion) {
    if (vacio(marca) || vacio(modelo) || vacio(descripcion)) {
      throw new IllegalArgumentException(
        "Completá los datos de tu equipo para solicitar el canje."
      );
    }
  }

  private void validarValorOfrecido(Double valorOfrecido) {
    if (valorOfrecido != null && valorOfrecido < 0) {
      throw new IllegalArgumentException("El valor estimado no puede ser negativo.");
    }
  }

  private ComputadoraUsada buscarComputadoraDisponible(Long computadoraId) {
    ComputadoraUsada computadora = repositorioCanje.buscarComputadora(computadoraId);
    if (computadora == null) {
      throw new IllegalArgumentException("La computadora seleccionada no existe.");
    }
    if (!Boolean.TRUE.equals(computadora.getActiva())) {
      throw new IllegalArgumentException("La computadora seleccionada no está disponible.");
    }
    if (computadora.getStock() == null || computadora.getStock() <= 0) {
      throw new IllegalArgumentException("La computadora seleccionada ya no tiene stock.");
    }
    return computadora;
  }

  private SolicitudCanje buscarSolicitudPendiente(Long solicitudId) {
    SolicitudCanje solicitud = repositorioCanje.buscarSolicitud(solicitudId);
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
    ComputadoraUsada computadora = repositorioCanje.buscarComputadoraConBloqueo(
      solicitud.getComputadora().getId()
    );
    validarStockAprobacion(computadora);
    computadora.setStock(computadora.getStock() - 1);
    solicitud.setValorAprobado(valorAprobado);
    solicitud.setEstado(EstadoSolicitudCanje.APROBADA);
    repositorioCanje.guardarComputadora(computadora);
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
    if (computadora == null) {
      throw new IllegalStateException("La computadora ya no está disponible.");
    }
    if (!Boolean.TRUE.equals(computadora.getActiva())) {
      throw new IllegalStateException("La computadora ya no está disponible.");
    }
    if (computadora.getStock() == null || computadora.getStock() < 1) {
      throw new IllegalStateException("La computadora ya no tiene unidades disponibles.");
    }
  }

  private boolean vacio(String valor) {
    return valor == null || valor.trim().isEmpty();
  }
}
