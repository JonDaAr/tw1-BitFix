package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.FechaEntregaNoDefinidaException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DatosCotizacionOnline;
import com.tallerwebi.presentacion.DatosOrden;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@SuppressWarnings("PMD.CyclomaticComplexity")
public class ServicioOrdenReparacionImpl implements ServicioOrdenReparacion {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServicioOrdenReparacionImpl.class);
  private static final int LIMITE_PARTES_NOMBRE = 2;
  private static final int INDICE_APELLIDO = 1;
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

    servicioHistorialOrden.registrarEvento(orden, orden.getEstado());

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
    if (orden == null) {
      throw new DatosIncompletosException();
    }
    validarCamposObligatorios(
      orden.getNombreCliente(),
      orden.getEmailCliente(),
      orden.getModeloEquipo(),
      orden.getDescripcionFalla()
    );
  }

  private void validarCamposObligatorios(String... campos) throws DatosIncompletosException {
    for (String campo : campos) {
      if (campo == null || campo.trim().isEmpty()) {
        throw new DatosIncompletosException();
      }
    }
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
    List<OrdenReparacion> ordenesDelTecnico = repositorioOrdenReparacion.buscarPorTecnico(
      tecnicoId
    );

    // Filtramos para que el técnico NUNCA vea las órdenes en cotización previa o esperando ingreso
    return ordenesDelTecnico
      .stream()
      .filter(orden ->
        orden.getEstado() != EstadoOrden.COTIZACION_PREVIA_ENVIADA &&
        orden.getEstado() != EstadoOrden.ESPERANDO_INGRESO
      )
      .collect(Collectors.toList());
  }

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
  public List<EstadoOrden> obtenerEstadosPermitidosPara(OrdenReparacion orden) {
    List<EstadoOrden> permitidos = new ArrayList<>();
    permitidos.add(orden.getEstado());

    ofrecerEstadosParaFlujoRecepcionTaller(orden, permitidos);

    return permitidos;
  }

  private void ofrecerEstadosParaFlujoRecepcionTaller(
    OrdenReparacion orden,
    List<EstadoOrden> permitidos
  ) {
    switch (orden.getEstado()) {
      case ESPERANDO_INGRESO:
      case COTIZACION_PREVIA_ENVIADA:
        evaluarPermisoDeIngreso(orden, permitidos);
        break;
      case RECIBIDO:
        permitidos.add(EstadoOrden.EN_DIAGNOSTICO);
        permitidos.add(EstadoOrden.CANCELADO);
        break;
      case EN_DIAGNOSTICO:
        permitidos.add(EstadoOrden.CANCELADO);
        break;
      default:
        ofrecerEstadosParaFlujoReparacion(orden.getEstado(), permitidos);
        break;
    }
  }

  private void evaluarPermisoDeIngreso(OrdenReparacion orden, List<EstadoOrden> permitidos) {
    if (orden.getSolicitaRetiro() == null || !orden.getSolicitaRetiro()) {
      permitidos.add(EstadoOrden.RECIBIDO);
    }
  }

  private void ofrecerEstadosParaFlujoReparacion(
    EstadoOrden estadoActual,
    List<EstadoOrden> permitidos
  ) {
    switch (estadoActual) {
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
        ofrecerEstadosParaFlujoPresupuestoYCierre(estadoActual, permitidos);
        break;
    }
  }

  private void ofrecerEstadosParaFlujoPresupuestoYCierre(
    EstadoOrden estadoActual,
    List<EstadoOrden> permitidos
  ) {
    switch (estadoActual) {
      case COTIZACION_PREVIA_ENVIADA:
        permitidos.add(EstadoOrden.COTIZACION_DESCARTADA);
        break;
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

    if (!orden.getEmailCliente().equals(emailUsuario)) {
      throw new SecurityException("Acceso denegado.");
    }

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

  @Override
  public List<OrdenReparacion> obtenerHistorialDelTecnico(Long idTecnico) {
    return repositorioOrdenReparacion.buscarOrdenesCompletadasPorTecnico(idTecnico);
  }

  @Override
  public OrdenReparacion registrarCotizacionOnlineParaTriaje(DatosCotizacionOnline datosCotizacion)
    throws DatosIncompletosException {
    if (datosCotizacion == null) {
      throw new DatosIncompletosException();
    }

    validarCamposObligatorios(
      datosCotizacion.getNombreCliente(),
      datosCotizacion.getEmailCliente(),
      datosCotizacion.getTelefonoCliente(),
      datosCotizacion.getModeloEquipo(),
      datosCotizacion.getDescripcionFalla()
    );

    OrdenReparacion nuevaOrden = new OrdenReparacion();
    nuevaOrden.setNombreCliente(datosCotizacion.getNombreCliente());
    nuevaOrden.setEmailCliente(datosCotizacion.getEmailCliente());
    nuevaOrden.setTelefonoCliente(datosCotizacion.getTelefonoCliente());
    nuevaOrden.setModeloEquipo(datosCotizacion.getModeloEquipo());
    nuevaOrden.setDescripcionFalla(datosCotizacion.getDescripcionFalla());
    nuevaOrden.setFechaIngreso(LocalDateTime.now());

    if (datosCotizacion.getAdjuntos() != null && datosCotizacion.getAdjuntos().length > 0) {
      String rutasGuardadas = guardarArchivosAdjuntos(datosCotizacion.getAdjuntos());
      nuevaOrden.setRutaArchivosAdjuntos(rutasGuardadas);
    }

    if (Boolean.TRUE.equals(datosCotizacion.getSolicitaRetiro())) {
      nuevaOrden.setSolicitaRetiro(true);
      nuevaOrden.setDireccionRetiro(datosCotizacion.getDireccionCliente());
      nuevaOrden.setLatitudRetiro(datosCotizacion.getLatitudCliente());
      nuevaOrden.setLongitudRetiro(datosCotizacion.getLongitudCliente());
      nuevaOrden.setCostoLogistica(datosCotizacion.getCostoLogistica());
    } else {
      nuevaOrden.setSolicitaRetiro(false);
      nuevaOrden.setCostoLogistica(0.0);
    }

    nuevaOrden.setEstado(EstadoOrden.PENDIENTE_TRIAJE);
    nuevaOrden.setNivelReparacion(null);
    nuevaOrden.setTecnicoAsignado(null);
    nuevaOrden.generarCodigoSeguimientoUnico();

    this.repositorioOrdenReparacion.guardarOrden(nuevaOrden);
    servicioHistorialOrden.registrarEvento(nuevaOrden, nuevaOrden.getEstado());

    return nuevaOrden;
  }

  private String guardarArchivosAdjuntos(
    org.springframework.web.multipart.MultipartFile... archivos
  ) {
    if (archivos == null || archivos.length == 0) return null;

    List<String> nombresGuardados = new ArrayList<>();
    String directorioDestino = "src/main/webapp/resources/uploads/";
    java.io.File directorio = new java.io.File(directorioDestino);
    if (!directorio.exists()) {
      directorio.mkdirs();
    }

    for (org.springframework.web.multipart.MultipartFile archivo : archivos) {
      if (!archivo.isEmpty()) {
        try {
          String extension = "";
          String nombreOriginal = archivo.getOriginalFilename();
          if (nombreOriginal != null && nombreOriginal.contains(".")) {
            extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
          }

          String nombreUnico = java.util.UUID.randomUUID().toString() + extension;
          java.nio.file.Path rutaFinal = java.nio.file.Paths.get(directorioDestino + nombreUnico);

          java.nio.file.Files.write(rutaFinal, archivo.getBytes());
          nombresGuardados.add(nombreUnico);
        } catch (java.io.IOException e) {
          if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Error al guardar archivo adjunto: {}", e.getMessage());
          }
        }
      }
    }
    return String.join(",", nombresGuardados);
  }

  @Override
  public void reasignarOrden(Long idOrden, Long idNuevoTecnico) {
    OrdenReparacion orden = repositorioOrdenReparacion.buscarPorId(idOrden);
    Usuario nuevoTecnico = repositorioUsuario.buscarPorId(idNuevoTecnico);

    if (orden == null || nuevoTecnico == null) {
      throw new IllegalArgumentException("Datos inválidos para la reasignación.");
    }

    orden.setTecnicoAsignado(nuevoTecnico);
    orden.setFechaAsignacion(LocalDateTime.now());

    repositorioOrdenReparacion.modificarOrden(orden);
  }

  @Override
  public void derivarCotizacion(
    Long idOrden,
    NivelReparacion nivelReparacion,
    Double estimadoMinimo,
    Double estimadoMaximo
  ) throws NoHayTecnicosDisponibles {
    OrdenReparacion orden = this.repositorioOrdenReparacion.buscarPorId(idOrden);

    if (orden == null) {
      throw new IllegalArgumentException("No se encontró la orden a derivar.");
    }

    orden.setNivelReparacion(nivelReparacion);
    orden.setEstimadoMinimo(estimadoMinimo);
    orden.setEstimadoMaximo(estimadoMaximo);

    // Asignamos el técnico preventivamente pero la orden queda en estado de cotización previa enviada
    this.servicioAsignacionTecnico.asignarTecnicoAOrden(orden);
    orden.setEstado(EstadoOrden.COTIZACION_PREVIA_ENVIADA);

    servicioHistorialOrden.registrarEvento(orden, EstadoOrden.COTIZACION_PREVIA_ENVIADA);
    this.repositorioOrdenReparacion.modificarOrden(orden);
  }

  @Override
  public OrdenReparacion registrarOrdenEnLocal(DatosOrden datos)
    throws DatosIncompletosException, NoHayTecnicosDisponibles {
    if (datos == null) {
      throw new DatosIncompletosException();
    }

    validarCamposObligatorios(
      datos.getNombreCliente(),
      datos.getEmailCliente(),
      datos.getTelefonoCliente(),
      datos.getModeloEquipo(),
      datos.getDescripcionFalla()
    );

    Usuario cliente = repositorioUsuario.buscar(datos.getEmailCliente());

    if (cliente == null) {
      cliente = new Usuario();
      cliente.setEmail(datos.getEmailCliente());

      // Separar Nombre y Apellido
      String[] partesNombre = datos.getNombreCliente().trim().split(" ", LIMITE_PARTES_NOMBRE);
      cliente.setNombre(partesNombre[0]); // El índice 0 está permitido por PMD

      if (partesNombre.length > INDICE_APELLIDO) {
        cliente.setApellido(partesNombre[INDICE_APELLIDO]);
      }

      cliente.setTelefono(datos.getTelefonoCliente()); // Guardamos el teléfono en BD
      cliente.setRol("CLIENTE");
      cliente.setActivo(true);

      String passwordGenerada = java.util.UUID.randomUUID().toString().substring(0, 8);
      cliente.setPassword(passwordGenerada);

      repositorioUsuario.guardar(cliente);

      // Pasamos la clave al DTO para que el controlador la pueda imprimir en pantalla
      datos.setPasswordProvisoria(passwordGenerada);

      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn(
          "NUEVO CLIENTE LOCAL CREADO - Destino: {} | Clave: {}",
          datos.getEmailCliente(),
          passwordGenerada
        );
      }
    }

    // 2. Crear la Orden Física
    OrdenReparacion nuevaOrden = new OrdenReparacion();
    nuevaOrden.setNombreCliente(datos.getNombreCliente());
    nuevaOrden.setEmailCliente(datos.getEmailCliente());
    nuevaOrden.setTelefonoCliente(datos.getTelefonoCliente());
    nuevaOrden.setModeloEquipo(datos.getModeloEquipo());
    nuevaOrden.setDescripcionFalla(datos.getDescripcionFalla());
    nuevaOrden.setNivelReparacion(datos.getNivelReparacion());

    nuevaOrden.setEstado(EstadoOrden.PENDIENTE_COTIZACION);

    return this.registrarOrden(nuevaOrden);
  }

  @Override
  public List<OrdenReparacion> obtenerOrdenesPendientesDeRetiro() {
    return repositorioOrdenReparacion.buscarOrdenesPendientesDeRetiro();
  }

  @Override
  public List<OrdenReparacion> obtenerOrdenesPendientesDeEntrega() {
    return repositorioOrdenReparacion.buscarOrdenesPendientesDeEntrega();
  }

  @Override
  public List<OrdenReparacion> obtenerOrdenesEsperadasDelTecnico(Long tecnicoId) {
    List<OrdenReparacion> ordenesDelTecnico = repositorioOrdenReparacion.buscarPorTecnico(
      tecnicoId
    );
    return ordenesDelTecnico
      .stream()
      .filter(orden -> orden.getEstado() == EstadoOrden.ESPERANDO_INGRESO)
      .collect(java.util.stream.Collectors.toList());
  }
}
