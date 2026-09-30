package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import com.tallerwebi.dominio.excepcion.OrdenNoEncontrado;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DatosOrden;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ServicioOrdenReparacion")
@Transactional
public class ServicioOrdenReparacionImpl implements ServicioOrdenReparacion {

  private RepositorioOrdenReparacion repositorioOrdenReparacion;
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
  public OrdenReparacion registrarOrden(DatosOrden orden) throws DatosIncompletosException {
    if (
      orden == null ||
      orden.getNombreCliente().trim().isEmpty() ||
      orden.getTelefonoCliente().trim().isEmpty() ||
      orden.getModeloEquipo().trim().isEmpty() ||
      orden.getDescripcionFalla().trim().isEmpty()
    ) {
      throw new DatosIncompletosException();
    } else {
      OrdenReparacion nuevaOrdenReparacion = new OrdenReparacion();
      nuevaOrdenReparacion.setNombreCliente(orden.getNombreCliente());
      nuevaOrdenReparacion.setTelefonoCliente(orden.getTelefonoCliente());
      nuevaOrdenReparacion.setModeloEquipo(orden.getModeloEquipo());
      nuevaOrdenReparacion.setDescripcionFalla(orden.getDescripcionFalla());

      nuevaOrdenReparacion.generarCodigoSeguimientoUnico();

      this.repositorioOrdenReparacion.guardarOrden(nuevaOrdenReparacion);
      return nuevaOrdenReparacion;
    }
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

  @Override
  public OrdenReparacion registrarOrden(OrdenReparacion orden) {
    List<Usuario> tecnicos = repositorioUsuario.buscarTecnicosActivos();

    if (tecnicos.isEmpty()) {
      throw new NoHayTecnicosDisponibles();
    }

    Usuario tecnicoSeleccionado = seleccionarTecnico(tecnicos);

    orden.setTecnicoAsignado(tecnicoSeleccionado);
    orden.setFechaAsignacion(LocalDateTime.now());

    repositorioOrdenReparacion.guardarOrden(orden);

    return orden;
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
      throw new OrdenNoEncontrado();
    }
    ordenSeleccionada.setEstado(nuevoEstado);
    if (notaTecnica != null && !notaTecnica.isEmpty()) {
      ordenSeleccionada.setNotaTecnica(notaTecnica);
    }
  }
}
