package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DatosOrden;

import java.util.List;

public interface ServicioOrdenReparacion {
  OrdenReparacion registrarOrden(DatosOrden orden) throws DatosIncompletosException;
  OrdenReparacion consultarEstado(Integer codigo) throws PedidoNoEncontradoException;
  OrdenReparacion registrarOrden(OrdenReparacion orden);

  //--------------Gestion estado y diagnostico("nota tecnica")
  List<OrdenReparacion> listarTodas();
  OrdenReparacion buscarPorId(Long idOrden);
  void actualizarEstadoYNotaTecnica(Long idOrdenReparacion, EstadoOrden nuevoEstado, String notaTecnica);

}
