package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import com.tallerwebi.dominio.excepcion.OrdenNoEncontrado;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DatosOrden;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ServicioOrdenReparacion")
@Transactional
public class ServicioOrdenReparacionImpl implements ServicioOrdenReparacion {

  private final RepositorioOrdenReparacion repositorioOrdenReparacion;
  private final RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioOrdenReparacionImpl(
    RepositorioUsuario repositorioUsuario,
    RepositorioOrdenReparacion repositorioOrdenReparacion
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioOrdenReparacion = repositorioOrdenReparacion;
  }

  @Override
  public OrdenReparacion registrarOrden(DatosOrden datosOrden)
    throws DatosIncompletosException, NoHayTecnicosDisponibles {
    validarDatosOrden(datosOrden);

    OrdenReparacion orden = new OrdenReparacion();
    orden.setNombreCliente(datosOrden.getNombreCliente());
    orden.setTelefonoCliente(datosOrden.getTelefonoCliente());
    orden.setEmailCliente(datosOrden.getEmailCliente());
    orden.setModeloEquipo(datosOrden.getModeloEquipo());
    orden.setDescripcionFalla(datosOrden.getDescripcionFalla());

    return registrarOrden(orden);
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

    asignarTecnicoAOrden(orden);

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

  private void validarDatosOrden(DatosOrden datosOrden) throws DatosIncompletosException {
    if (
      datosOrden == null ||
      esCadenaVacia(datosOrden.getNombreCliente()) ||
      esCadenaVacia(datosOrden.getEmailCliente()) ||
      esCadenaVacia(datosOrden.getModeloEquipo()) ||
      esCadenaVacia(datosOrden.getDescripcionFalla())
    ) {
      throw new DatosIncompletosException();
    }
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

  private void asignarTecnicoAOrden(OrdenReparacion orden) {
    List<Usuario> tecnicos = repositorioUsuario.buscarTecnicosActivos();
    if (tecnicos.isEmpty()) {
      throw new NoHayTecnicosDisponibles();
    }

    Usuario tecnicoSeleccionado = seleccionarTecnico(tecnicos);
    orden.setTecnicoAsignado(tecnicoSeleccionado);
    orden.setFechaAsignacion(LocalDateTime.now());
  }

  private boolean esCadenaVacia(String texto) {
    return texto == null || texto.trim().isEmpty();
  }

  private Usuario seleccionarTecnico(List<Usuario> tecnicos) {
    Usuario seleccionado = tecnicos.get(0);
    long menorCantidad = repositorioOrdenReparacion.contarOrdenesActivas(seleccionado.getId());
    LocalDateTime ultimaAsignacion = repositorioOrdenReparacion.buscarFechaUltimaAsignacion(
      seleccionado.getId()
    );

    for (int i = 1; i < tecnicos.size(); i++) {
      Usuario candidato = tecnicos.get(i);
      long cantidad = repositorioOrdenReparacion.contarOrdenesActivas(candidato.getId());
      LocalDateTime ultimaAsignacionCandidato =
        repositorioOrdenReparacion.buscarFechaUltimaAsignacion(candidato.getId());

      if (debeSeleccionarse(cantidad, ultimaAsignacionCandidato, menorCantidad, ultimaAsignacion)) {
        seleccionado = candidato;
        menorCantidad = cantidad;
        ultimaAsignacion = ultimaAsignacionCandidato;
      }
    }

    return seleccionado;
  }

  private boolean debeSeleccionarse(
    long cantidadCandidato,
    LocalDateTime ultimaAsignacionCandidato,
    long menorCantidad,
    LocalDateTime ultimaAsignacionSeleccionado
  ) {
    if (cantidadCandidato < menorCantidad) {
      return true;
    }
    if (cantidadCandidato > menorCantidad) {
      return false;
    }
    if (ultimaAsignacionSeleccionado == null) {
      return false;
    }
    return (
      ultimaAsignacionCandidato == null ||
      ultimaAsignacionCandidato.isBefore(ultimaAsignacionSeleccionado)
    );
  }

  @Override
  public List<OrdenReparacion> obtenerOrdenesParaTecnico() {
    return repositorioOrdenReparacion.buscarTodas();
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

    ordenSeleccionada.setEstado(nuevoEstado);
    if (notaTecnica != null && !notaTecnica.isEmpty()) {
      ordenSeleccionada.setNotaTecnica(notaTecnica);
    }
  }

  @Override
  public List<EstadoOrden> obtenerEstadosPermitidosPara(EstadoOrden estadoActual) {
    List<EstadoOrden> permitidos = new ArrayList<>();
    permitidos.add(estadoActual);

    switch (estadoActual) {
      case RECIBIDO:
        permitidos.add(EstadoOrden.EN_DIAGNOSTICO);
        break;
      case EN_DIAGNOSTICO:
        permitidos.add(EstadoOrden.ESPERANDO_REPUESTO);
        permitidos.add(EstadoOrden.REPARADO);
        break;
      case ESPERANDO_REPUESTO:
        permitidos.add(EstadoOrden.REPARADO);
        break;
      case REPARADO:
        // Estado final: solo se devuelve a sí mismo
        break;
      case ENTREGADO:
        // Estado final: solo se devuelve a sí mismo
        break;
      default:
        break;
    }
    return permitidos;
  }

  @Override
  public void aceptarPresupuesto(Integer codigoSeguimiento) {
    OrdenReparacion orden = repositorioOrdenReparacion.buscarPorCodigo(codigoSeguimiento);

    if (orden == null) {
      throw new IllegalArgumentException(
        "No se encontró la orden con el código de seguimiento: " + codigoSeguimiento
      );
    }

    orden.setEstado(EstadoOrden.PRESUPUESTO_ACEPTADO);

    repositorioOrdenReparacion.modificarOrden(orden);
  }
}
