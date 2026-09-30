package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DatosOrden;
import java.util.List;

public interface ServicioOrdenReparacion {
  OrdenReparacion registrarOrden(DatosOrden datosOrden)
    throws DatosIncompletosException, NoHayTecnicosDisponibles;

  OrdenReparacion registrarOrden(OrdenReparacion orden)
    throws DatosIncompletosException, NoHayTecnicosDisponibles;

  OrdenReparacion consultarEstado(Integer codigo) throws PedidoNoEncontradoException;

  List<OrdenReparacion> obtenerOrdenesParaTecnico();
  //--------------Gestion estado y diagnostico("nota tecnica")
  List<OrdenReparacion> listarTodas();
  OrdenReparacion buscarPorId(Long idOrden);
  void actualizarEstadoYNotaTecnica(
    Long idOrdenReparacion,
    EstadoOrden nuevoEstado,
    String notaTecnica
  );
}
