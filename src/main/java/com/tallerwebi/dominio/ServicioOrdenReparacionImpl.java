package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DatosOrden;
import jakarta.transaction.Transactional;
import java.util.Locale;
import org.hibernate.SessionFactory;
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
}
