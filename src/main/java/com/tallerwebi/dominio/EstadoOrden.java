package com.tallerwebi.dominio;

import java.util.List;
import java.util.ArrayList;

public enum EstadoOrden {
  //Flujo Tradicional (Se lleva el equipo al taller y se recepciona)
  RECIBIDO("RECIBIDO"),
  EN_DIAGNOSTICO("EN DIAGNOSTICO"),
  ESPERANDO_REPUESTO("ESPERANDO REPUESTO"),
  PRESUPUESTO_ENVIADO("PRESUPUESTO ENVIADO"),
  PRESUPUESTO_ACEPTADO("PRESUPUESTO ACEPTADO"),
  PRESUPUESTO_RECHAZADO("PRESUPUESTO RECHAZADO"),
  REPARADO("REPARADO"),
  ENTREGADO("ENTREGADO"),
  EN_REPARACION("EN REPARACION"),

  // Nuevo Flujo Cotizacion Online (Previo al ingreso al taller)
  SOLICITUD_COTIZACION("SOLICITUD DE COTIZACION ONLINE"),
  COTIZACION_PREVIA_ENVIADA("COTIZACION PREVIA ENVIADA"),
  ESPERANDO_INGRESO("ESPERANDO INGRESO"),
  COTIZACION_DESCARTADA("COTIZACION DESCARTADA");

  private final String descripcion;

  EstadoOrden(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public static List<EstadoOrden> estadosFinales() {
    return List.of(ENTREGADO, PRESUPUESTO_RECHAZADO, COTIZACION_DESCARTADA);
  }

  public boolean esActivo() {
    return !estadosFinales().contains(this);
  }
  public List<EstadoOrden> estadosPermitidos() {
    List<EstadoOrden> permitidos = new ArrayList<>();
    permitidos.add(this);
    ofrecerEstadosParaFlujoCotizacionOnline(permitidos);
    ofrecerEstadosParaFlujoRecepcionTaller(permitidos);
    return permitidos;
  }
  private void ofrecerEstadosParaFlujoCotizacionOnline(
    List<EstadoOrden> permitidos
  ) {
    switch (this) {
      // Flujo Cotizacion Online
      case SOLICITUD_COTIZACION: // El cliente inicia solicitando cotizacion online
        permitidos.add(EstadoOrden.COTIZACION_PREVIA_ENVIADA);
        break;
      case COTIZACION_PREVIA_ENVIADA: // El tecnico responde y cambia a este estado
        permitidos.add(EstadoOrden.ESPERANDO_INGRESO);
        permitidos.add(EstadoOrden.COTIZACION_DESCARTADA);
        break;
      case ESPERANDO_INGRESO: // Cliente acepta la cotizacion y debe llevar el equipo al taller para esperar el presupuesto final
        permitidos.add(EstadoOrden.RECIBIDO); // El tecnico lo recibe y se conecta con el flujo tradicional
        break;
      case COTIZACION_DESCARTADA:
        break; // El cliente rechazo la cotizacion y no va a llevar el equipo a reparar
      default:
        break;
    }
  }

  private void ofrecerEstadosParaFlujoRecepcionTaller(
    List<EstadoOrden> permitidos
  ) {
    switch (this) {
      // Flujo tradicional - recepcion en el taller
      case RECIBIDO:
        permitidos.add(EstadoOrden.EN_DIAGNOSTICO);
        break;
      case EN_DIAGNOSTICO:
        permitidos.add(EstadoOrden.ESPERANDO_REPUESTO);
        permitidos.add(EstadoOrden.REPARADO);
        break;
      case PRESUPUESTO_ACEPTADO:
        permitidos.add(EstadoOrden.EN_REPARACION);
        permitidos.add(EstadoOrden.ESPERANDO_REPUESTO);
        break;
      case EN_REPARACION:
        permitidos.add(EstadoOrden.REPARADO);
        permitidos.add(EstadoOrden.ESPERANDO_REPUESTO);
        break;
      case ESPERANDO_REPUESTO:
        permitidos.add(EstadoOrden.REPARADO);
        permitidos.add(EstadoOrden.EN_REPARACION);
        break;
      case REPARADO:
        permitidos.add(EstadoOrden.ENTREGADO);
        break;
      case ENTREGADO:
        break;
      default:
        break;
    }
  }

}
