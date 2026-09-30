package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DatosOrden;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
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
      orden.setEstado("PENDIENTE");
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
}
