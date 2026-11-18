package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.presentacion.DatosCotizacionOnline;
import com.tallerwebi.presentacion.DatosOrden;
import java.util.List;

public interface ServicioOrdenReparacion {
  OrdenReparacion registrarOrden(OrdenReparacion orden)
    throws DatosIncompletosException, NoHayTecnicosDisponibles;
  OrdenReparacion consultarEstado(Integer codigo) throws PedidoNoEncontradoException;
  List<OrdenReparacion> obtenerOrdenesParaTecnico();
  List<OrdenReparacion> listarTodas();
  OrdenReparacion buscarPorId(Long idOrden);
  void actualizarEstadoYNotaTecnica(
    Long idOrdenReparacion,
    EstadoOrden nuevoEstado,
    String notaTecnica
  );
  List<EstadoOrden> obtenerEstadosPermitidosPara(OrdenReparacion orden);
  void aceptarPresupuesto(Integer codigoSeguimiento, String emailUsuario);
  void rechazarPresupuesto(Integer codigoSeguimiento, String emailUsuario);
  List<OrdenReparacion> obtenerOrdenesDelCliente(String email);
  List<OrdenReparacion> obtenerOrdenesDelTecnico(Long tecnicoId);
  List<OrdenReparacion> obtenerHistorialDelTecnico(Long idTecnico);
  List<OrdenReparacion> obtenerOrdenesEsperadasDelTecnico(Long tecnicoId);
  void reasignarOrden(Long idOrden, Long idTecnico);
  OrdenReparacion registrarCotizacionOnlineParaTriaje(DatosCotizacionOnline datosCotizacion)
    throws DatosIncompletosException;
  void derivarCotizacion(
    Long idOrden,
    NivelReparacion nivelReparacion,
    Double estimadoMinimo,
    Double estimadoMaximo
  ) throws NoHayTecnicosDisponibles;
  OrdenReparacion registrarOrdenEnLocal(DatosOrden datos)
    throws DatosIncompletosException, NoHayTecnicosDisponibles;
  List<OrdenReparacion> obtenerOrdenesPendientesDeRetiro();
  List<OrdenReparacion> obtenerOrdenesPendientesDeEntrega();
}
