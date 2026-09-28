package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DatosOrden;

public interface ServicioOrdenReparacion {
  OrdenReparacion registrarOrden(DatosOrden orden) throws DatosIncompletosException;
  OrdenReparacion consultarEstado(Integer codigo) throws PedidoNoEncontradoException;
}
