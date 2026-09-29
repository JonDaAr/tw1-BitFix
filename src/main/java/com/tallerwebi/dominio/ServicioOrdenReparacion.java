package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.presentacion.DatosOrden;
import java.util.List;

//@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ServicioOrdenReparacion {
  OrdenReparacion registrarOrden(DatosOrden orden) throws DatosIncompletosException;

  //
  OrdenReparacion buscarPorId(Long idOrdenReparacion);
  List<OrdenReparacion> listarTodas();
  void actualizarEstadoYNotaTecnica(
    Long idOrdenReparacion,
    EstadoOrden nuevoEstado,
    String notaTecnica
  );
}
