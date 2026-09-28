package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EstadoInvalidoParaCierreException;
import com.tallerwebi.dominio.excepcion.OrdenNoEncontradaException;

public interface ServicioCierreOrden {
  OrdenReparacion cerrarOrden(Long idOrden)
    throws OrdenNoEncontradaException, EstadoInvalidoParaCierreException;
  OrdenReparacion obtenerOrdenParaComprobante(Long idOrden) throws OrdenNoEncontradaException;
  OrdenReparacion crearOrdenPruebaReparada();
}
