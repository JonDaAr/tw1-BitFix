package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.OrdenNoEncontrado;
import com.tallerwebi.presentacion.DatosOrden;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ServicioOrdenReparacion")
@Transactional
public class ServicioOrdenReparacionImpl implements ServicioOrdenReparacion {

  private RepositorioOrdenReparacion repositorioOrdenReparacion;

  @Autowired
  public ServicioOrdenReparacionImpl(RepositorioOrdenReparacion repositorioOrdenReparacion) {
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

  //
  @Override
  public OrdenReparacion buscarPorId(Long idOrdenReparacion) {
    return repositorioOrdenReparacion.buscarPorIdOrdenReparacion(idOrdenReparacion);
  }

  @Override
  public List<OrdenReparacion> listarTodas() {
    return repositorioOrdenReparacion.listarTodasLasOrdenes();
  }

  @Override
  public void actualizarEstadoYNotaTecnica(
    Long idOrdenReparacion,
    EstadoOrden nuevoEstado,
    String notaTecnica
  ) {
    OrdenReparacion ordenSeleccionada = repositorioOrdenReparacion.buscarPorIdOrdenReparacion(
      idOrdenReparacion
    );
    if (ordenSeleccionada == null) {
      throw new OrdenNoEncontrado();
    }
    ordenSeleccionada.setEstado(nuevoEstado);
    if (notaTecnica != null && !notaTecnica.isEmpty()) {
      ordenSeleccionada.setNotaTecnica(notaTecnica);
    }
  }
}
