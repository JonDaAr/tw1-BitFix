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

@Service
@Transactional
public class ServicioOrdenReparacionImpl implements ServicioOrdenReparacion {

  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioOrdenReparacion repositorioOrdenReparacion;
  private final ServicioAsignacionTecnico servicioAsignacionTecnico;
  private final ServicioHistorialOrden servicioHistorialOrden;
  private final ServicioPresupuesto servicioPresupuesto;

  @Autowired
  public ServicioOrdenReparacionImpl(
    RepositorioUsuario repositorioUsuario,
    RepositorioOrdenReparacion repositorioOrdenReparacion,
    ServicioAsignacionTecnico servicioAsignacionTecnico,
    ServicioHistorialOrden servicioHistorialOrden,
    ServicioPresupuesto servicioPresupuesto
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioOrdenReparacion = repositorioOrdenReparacion;
    this.servicioAsignacionTecnico = servicioAsignacionTecnico;
    this.servicioHistorialOrden = servicioHistorialOrden;
    this.servicioPresupuesto = servicioPresupuesto;
  }

  @Override
  public OrdenReparacion registrarOrden(OrdenReparacion orden)
    throws DatosIncompletosException, NoHayTecnicosDisponibles {
    validarOrden(orden);

    if (orden.getCodigoSeguimiento() == null) {
      orden.generarCodigoSeguimientoUnico();
    }

    if (orden.getEstado() == null) {
      orden.setEstado(EstadoOrden.REPARADO);
    }

    this.servicioAsignacionTecnico.asignarTecnicoAOrden(orden);

    this.repositorioOrdenReparacion.guardarOrden(orden);

    servicioHistorialOrden.registrarEvento(orden, orden.getEstado()); //para guardar en el historial para el timeline

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

    if (ordenSeleccionada.getEstado() != nuevoEstado) {
      if (
        ordenSeleccionada.getEstado() == EstadoOrden.PRESUPUESTO_ENVIADO &&
        nuevoEstado == EstadoOrden.PRESUPUESTO_RECHAZADO
      ) {
        servicioPresupuesto.devolverStockPorOrden(idOrdenReparacion);
      }

      ordenSeleccionada.setEstado(nuevoEstado);
      if (
        nuevoEstado == EstadoOrden.ENTREGADO || nuevoEstado == EstadoOrden.ENTREGADO_SIN_REPARAR
      ) {
        ordenSeleccionada.setFechaEntrega(LocalDateTime.now());
      }
      servicioHistorialOrden.registrarEvento(ordenSeleccionada, nuevoEstado);
    }

    if (notaTecnica != null && !notaTecnica.isEmpty()) {
      ordenSeleccionada.setNotaTecnica(notaTecnica);
    }
  }

  @Override
  public List<EstadoOrden> obtenerEstadosPermitidosPara(EstadoOrden estadoActual) {
    List<EstadoOrden> permitidos = new ArrayList<>();
    permitidos.add(estadoActual);

    ofrecerEstadosParaFlujoRecepcionTaller(estadoActual, permitidos);

    return permitidos;
  }

  private void ofrecerEstadosParaFlujoRecepcionTaller(
    EstadoOrden estadoActual,
    List<EstadoOrden> permitidos
  ) {
    switch (estadoActual) {
      // Flujo tradicional - recepcion en el taller
      case ESPERANDO_INGRESO:
        permitidos.add(EstadoOrden.RECIBIDO);
        break;
      case RECIBIDO:
        permitidos.add(EstadoOrden.EN_DIAGNOSTICO);
        permitidos.add(EstadoOrden.CANCELADO);
        break;
      case EN_DIAGNOSTICO:
        permitidos.add(EstadoOrden.CANCELADO);
        break;
      case PRESUPUESTO_ACEPTADO:
        permitidos.add(EstadoOrden.EN_REPARACION);
        break;
      case EN_REPARACION:
        permitidos.add(EstadoOrden.ESPERANDO_REPUESTO);
        permitidos.add(EstadoOrden.REPARADO);
        break;
      case ESPERANDO_REPUESTO:
        permitidos.add(EstadoOrden.EN_REPARACION);
        break;
      case REPARADO:
        permitidos.add(EstadoOrden.ENTREGADO);
        break;
      default:
        ofrecerEstadosParaFlujoPresupuesto(estadoActual, permitidos);
        break;
    }
  }

  private void ofrecerEstadosParaFlujoPresupuesto(
    EstadoOrden estadoActual,
    List<EstadoOrden> permitidos
  ) {
    switch (estadoActual) {
      case PRESUPUESTO_ENVIADO:
        permitidos.add(EstadoOrden.PRESUPUESTO_ACEPTADO);
        permitidos.add(EstadoOrden.PRESUPUESTO_RECHAZADO);
        break;
      case PRESUPUESTO_RECHAZADO:
      case CANCELADO:
        permitidos.add(EstadoOrden.ENTREGADO_SIN_REPARAR);
        break;
      default:
        break;
    }
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
    servicioHistorialOrden.registrarEvento(orden, EstadoOrden.PRESUPUESTO_ACEPTADO);
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

    servicioPresupuesto.devolverStockPorOrden(orden.getIdOrdenReparacion());
    orden.setEstado(EstadoOrden.PRESUPUESTO_RECHAZADO);
    servicioHistorialOrden.registrarEvento(orden, EstadoOrden.PRESUPUESTO_RECHAZADO);
    repositorioOrdenReparacion.modificarOrden(orden);
  }

  //Para Implementar Historial del Tecnico
  @Override
  public List<OrdenReparacion> obtenerHistorialDelTecnico(Long idTecnico) {
    return repositorioOrdenReparacion.buscarOrdenesCompletadasPorTecnico(idTecnico);
  }

  @Override
  public OrdenReparacion registrarCotizacionOnlineParaTriaje(DatosCotizacionOnline datosCotizacion)
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

    // Mapea DTO a Entidad
    OrdenReparacion nuevaOrden = new OrdenReparacion();
    nuevaOrden.setNombreCliente(datosCotizacion.getNombreCliente());
    nuevaOrden.setEmailCliente(datosCotizacion.getEmailCliente());
    nuevaOrden.setModeloEquipo(datosCotizacion.getModeloEquipo());
    nuevaOrden.setDescripcionFalla(datosCotizacion.getDescripcionFalla());
    // Agregamos la fecha de ingreso (clave para ordenar)
    nuevaOrden.setFechaIngreso(LocalDateTime.now());

    // ESTADO DE TRIAJE: El Técnico Senior la revisará.
    nuevaOrden.setEstado(EstadoOrden.PENDIENTE_TRIAJE);

    // NI TIER NI TECNICO ESTÁN ASIGNADOS AÚN.
    nuevaOrden.setNivelReparacion(null);
    nuevaOrden.setTecnicoAsignado(null);

    nuevaOrden.generarCodigoSeguimientoUnico();

    // Se guarda directo sin asignar a nadie.
    this.repositorioOrdenReparacion.guardarOrden(nuevaOrden);
    servicioHistorialOrden.registrarEvento(nuevaOrden, nuevaOrden.getEstado()); //registra evento para el timeline

    return nuevaOrden;
  }

  // Metodo obsoleto eliminado: registrarCotizacionOnline()

  @Override
  public void reasignarOrden(Long idOrden, Long idNuevoTecnico) {
    OrdenReparacion orden = repositorioOrdenReparacion.buscarPorId(idOrden);
    Usuario nuevoTecnico = repositorioUsuario.buscarPorId(idNuevoTecnico);

    if (orden == null || nuevoTecnico == null) {
      throw new IllegalArgumentException("Datos inválidos para la reasignación.");
    }

    // Hacemos el cambio de manos y reseteamos la fecha para el balanceo de carga
    orden.setTecnicoAsignado(nuevoTecnico);
    orden.setFechaAsignacion(LocalDateTime.now());

    repositorioOrdenReparacion.modificarOrden(orden);
  }

  @Override
  public void derivarCotizacion(Long idOrden, NivelReparacion nivelReparacion)
    throws NoHayTecnicosDisponibles {
    OrdenReparacion orden = this.repositorioOrdenReparacion.buscarPorId(idOrden);

    if (orden == null) {
      throw new IllegalArgumentException("No se encontró la orden a derivar.");
    }

    // 1. Seteamos el nivel de complejidad decidido por el Tier 3
    orden.setNivelReparacion(nivelReparacion);

    // 2. ¡ESTA ES LA LÍNEA QUE FALTABA PARA QUE PASE EL TEST!
    // Ejecutamos el algoritmo delegando al nuevo servicio
    this.servicioAsignacionTecnico.asignarTecnicoAOrden(orden);

    // 3. Avanzamos el estado de la orden
    orden.setEstado(EstadoOrden.PENDIENTE_COTIZACION);

    servicioHistorialOrden.registrarEvento(orden, EstadoOrden.PENDIENTE_COTIZACION); //registra evento para timeline
    // 4. Guardamos todos los cambios (incluido el técnico) en la base de datos
    this.repositorioOrdenReparacion.modificarOrden(orden);
  }
}
