package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.FechaEntregaNoDefinidaException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DatosCotizacionOnline;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("ServicioOrdenReparacion")
@Transactional
public class ServicioOrdenReparacionImpl implements ServicioOrdenReparacion {

  private final RepositorioOrdenReparacion repositorioOrdenReparacion;
  private final RepositorioUsuario repositorioUsuario;
  private final AsignadorTecnicos asignadorTecnicos;

  @Autowired
  public ServicioOrdenReparacionImpl(
    RepositorioUsuario repositorioUsuario,
    RepositorioOrdenReparacion repositorioOrdenReparacion
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.asignadorTecnicos = new AsignadorTecnicos(repositorioUsuario, repositorioOrdenReparacion);
    this.repositorioOrdenReparacion = repositorioOrdenReparacion;
  }

  @Override
  public OrdenReparacion registrarOrden(OrdenReparacion orden)
    throws DatosIncompletosException, NoHayTecnicosDisponibles {
    validarOrden(orden);

    asignarCodigoDisponible(orden);

    if (orden.getEstado() == null) {
      orden.setEstado(EstadoOrden.RECIBIDO);
    }

    asignadorTecnicos.asignar(orden);

    this.repositorioOrdenReparacion.guardarOrden(orden);

    return orden;
  }

  @Override
  public OrdenReparacion consultarEstado(Integer codigo) throws PedidoNoEncontradoException {
    if (codigo == null) {
      throw new PedidoNoEncontradoException("El código no puede estar vacío");
    }

    OrdenReparacion orden = repositorioOrdenReparacion.buscarPorCodigo(codigo);
    if (orden == null) {
      throw new PedidoNoEncontradoException("No se encontró ningún pedido con el código ingresado");
    }
    return orden;
  }

  private void validarOrden(OrdenReparacion orden) throws DatosIncompletosException {
    if (
      orden == null ||
      esCadenaVacia(orden.getNombreCliente()) ||
      esCadenaVacia(orden.getEmailCliente()) ||
      esCadenaVacia(orden.getModeloEquipo()) ||
      esCadenaVacia(orden.getDescripcionFalla())
    ) {
      throw new DatosIncompletosException();
    }
  }

  private void asignarCodigoDisponible(OrdenReparacion orden) {
    for (int intento = 0; intento < 32; intento++) {
      Integer codigo = orden.getCodigoSeguimiento();
      if (codigo != null && !repositorioOrdenReparacion.existeCodigo(codigo)) {
        return;
      }
      orden.setCodigoSeguimiento(orden.generarCodigoSeguimientoUnico());
    }
    throw new IllegalStateException("No se pudo generar un código de seguimiento disponible.");
  }

  private boolean esCadenaVacia(String texto) {
    return texto == null || texto.trim().isEmpty();
  }

  @Override
  public List<OrdenReparacion> obtenerOrdenesParaTecnico() {
    List<OrdenReparacion> listaOrdenes = new ArrayList<>(repositorioOrdenReparacion.buscarTodas());
    ordenarLista(listaOrdenes);
    return listaOrdenes;
  }

  private void ordenarLista(List<OrdenReparacion> listaOrdenes) {
    listaOrdenes.sort((a, b) -> {
      boolean aEntregada = a.getEstado() == EstadoOrden.ENTREGADO;
      boolean bEntregada = b.getEstado() == EstadoOrden.ENTREGADO;

      if (aEntregada && bEntregada) {
        if (a.getFechaEntrega() == null || b.getFechaEntrega() == null) {
          throw new FechaEntregaNoDefinidaException("La orden entregada no tiene fecha de entrega");
        }
        return a.getFechaEntrega().compareTo(b.getFechaEntrega());
      }
      if (aEntregada) {
        return 1;
      }
      if (bEntregada) {
        return -1;
      }
      int porPrioridad = a.getPrioridad().compareTo(b.getPrioridad());
      if (porPrioridad != 0) {
        return porPrioridad;
      }
      return a.getFechaIngreso().compareTo(b.getFechaIngreso());
    });
  }

  @Override
  public List<OrdenReparacion> obtenerOrdenesDelCliente(String email) {
    return repositorioOrdenReparacion.buscarPorEmailCliente(email);
  }

  @Override
  public List<OrdenReparacion> obtenerOrdenesDelTecnico(Long tecnicoId) {
    return repositorioOrdenReparacion.buscarPorTecnico(tecnicoId);
  }

  //--------------Gestion estado y diagnostico("nota tecnica")
  @Override
  public List<OrdenReparacion> listarTodas() {
    return repositorioOrdenReparacion.listarTodasLasOrdenes();
  }

  @Override
  public OrdenReparacion buscarPorId(Long idOrden) {
    return repositorioOrdenReparacion.buscarPorId(idOrden);
  }

  @Override
  public void actualizarEstadoYNotaTecnica(
    Long idOrdenReparacion,
    EstadoOrden nuevoEstado,
    String notaTecnica
  ) {
    OrdenReparacion ordenSeleccionada = repositorioOrdenReparacion.buscarPorId(idOrdenReparacion);
    if (ordenSeleccionada == null) {
      throw new IllegalArgumentException("No se encontró la orden.");
    }
    validarTransicion(ordenSeleccionada, nuevoEstado);

    // SI PASA A ENTREGADO, GUARDAMOS LA FECHA ACTUAL
    if (nuevoEstado == EstadoOrden.ENTREGADO && ordenSeleccionada.getEstado() != nuevoEstado) {
      ordenSeleccionada.setFechaEntrega(LocalDateTime.now());
    }
    ordenSeleccionada.setEstado(nuevoEstado);

    if (notaTecnica != null && !notaTecnica.isEmpty()) {
      ordenSeleccionada.setNotaTecnica(notaTecnica);
    }
  }

  private void validarTransicion(OrdenReparacion orden, EstadoOrden nuevoEstado) {
    if (nuevoEstado == null || !obtenerEstadosPermitidosPara(orden.getEstado()).contains(nuevoEstado)) {
      throw new IllegalStateException("El cambio de estado solicitado no está permitido.");
    }
    if (nuevoEstado == EstadoOrden.ENTREGADO && orden.getEstado() != nuevoEstado) {
      if (!orden.tieneMontoValido()) {
        throw new IllegalStateException("No se puede entregar una orden sin un presupuesto válido.");
      }
    }
  }

  @Override
  public List<EstadoOrden> obtenerEstadosPermitidosPara(EstadoOrden estadoActual) {
    return estadoActual == null ? new ArrayList<>() : estadoActual.estadosPermitidos();
  }

  @Override
  public void aceptarPresupuesto(Integer codigoSeguimiento, String emailUsuario) {
    OrdenReparacion orden = repositorioOrdenReparacion.buscarPorCodigo(codigoSeguimiento);

    if (orden == null) {
      throw new IllegalArgumentException("No se encontró la orden.");
    }

    // Validar IDOR
    if (!orden.getEmailCliente().equals(emailUsuario)) {
      throw new SecurityException("Acceso denegado.");
    }

    //Validar máquina de estados
    if (orden.getEstado() != EstadoOrden.PRESUPUESTO_ENVIADO) {
      throw new IllegalStateException(
        "El presupuesto no se puede aceptar en el estado actual: " + orden.getEstado()
      );
    }

    orden.setEstado(EstadoOrden.PRESUPUESTO_ACEPTADO);
    repositorioOrdenReparacion.modificarOrden(orden);
  }

  @Override
  public void rechazarPresupuesto(Integer codigoSeguimiento, String emailUsuario) {
    OrdenReparacion orden = repositorioOrdenReparacion.buscarPorCodigo(codigoSeguimiento);

    if (orden == null) {
      throw new IllegalArgumentException("No se encontró la orden.");
    }

    if (!orden.getEmailCliente().equals(emailUsuario)) {
      throw new SecurityException("Acceso denegado.");
    }

    //Validar máquina de estados
    if (orden.getEstado() != EstadoOrden.PRESUPUESTO_ENVIADO) {
      throw new IllegalStateException(
        "El presupuesto no se puede rechazar en el estado actual: " + orden.getEstado()
      );
    }

    // Las entidades reservadas siguen administradas por la misma transacción.
    for (ReservaRepuesto reserva : orden.getReservasPresupuesto()) {
      Repuesto repuesto = reserva.getRepuesto();
      repuesto.setStock(Math.addExact(repuesto.getStock(), reserva.getCantidad()));
    }
    orden.setEstado(EstadoOrden.PRESUPUESTO_RECHAZADO);
    repositorioOrdenReparacion.modificarOrden(orden);
  }

  //Para Implementar Historial del Tecnico
  @Override
  public List<OrdenReparacion> obtenerHistorialDelTecnico(Long idTecnico) {
    return repositorioOrdenReparacion.buscarOrdenesCompletadasPorTecnico(idTecnico);
  }

  @Override
  public OrdenReparacion registrarCotizacionOnline(DatosCotizacionOnline datosCotizacion)
    throws DatosIncompletosException {
    // Valida que no lleguen campos vacios
    if (
      datosCotizacion == null ||
      esCadenaVacia(datosCotizacion.getNombreCliente()) ||
      esCadenaVacia(datosCotizacion.getEmailCliente()) ||
      esCadenaVacia(datosCotizacion.getModeloEquipo()) ||
      esCadenaVacia(datosCotizacion.getDescripcionFalla())
    ) {
      throw new DatosIncompletosException();
    }

    //Mapea DTO a Entidad
    OrdenReparacion nuevaOrden = new OrdenReparacion();
    nuevaOrden.setNombreCliente(datosCotizacion.getNombreCliente());
    nuevaOrden.setEmailCliente(datosCotizacion.getEmailCliente());
    nuevaOrden.setModeloEquipo(datosCotizacion.getModeloEquipo());
    nuevaOrden.setDescripcionFalla(datosCotizacion.getDescripcionFalla());

    nuevaOrden.setEstado(EstadoOrden.SOLICITUD_COTIZACION);
    asignarCodigoDisponible(nuevaOrden);
    asignadorTecnicos.asignar(nuevaOrden);

    this.repositorioOrdenReparacion.guardarOrden(nuevaOrden);

    return nuevaOrden;
  }

  @Override
  public void reasignarOrden(Long idOrden, Long idNuevoTecnico) {
    OrdenReparacion orden = repositorioOrdenReparacion.buscarPorId(idOrden);
    Usuario nuevoTecnico = repositorioUsuario.buscarPorId(idNuevoTecnico);

    if (orden == null || nuevoTecnico == null) {
      throw new IllegalArgumentException("Datos inválidos para la reasignación.");
    }
    if (!orden.estaActiva()) {
      throw new IllegalStateException("No se puede reasignar una orden finalizada.");
    }
    validarTecnicoParaOrden(nuevoTecnico, orden);

    // Hacemos el cambio de manos y reseteamos la fecha para el balanceo de carga
    orden.setTecnicoAsignado(nuevoTecnico);
    orden.setFechaAsignacion(LocalDateTime.now());

    repositorioOrdenReparacion.modificarOrden(orden);
  }

  private void validarTecnicoParaOrden(Usuario tecnico, OrdenReparacion orden) {
    if (!"TECNICO".equalsIgnoreCase(tecnico.getRol()) || !Boolean.TRUE.equals(tecnico.getActivo())) {
      throw new IllegalArgumentException("El usuario seleccionado debe ser un técnico activo.");
    }
    if (tecnico.getNivelSoporte() != orden.getNivelReparacion()) {
      throw new IllegalArgumentException("El técnico debe tener el nivel requerido por la orden.");
    }
  }
}
