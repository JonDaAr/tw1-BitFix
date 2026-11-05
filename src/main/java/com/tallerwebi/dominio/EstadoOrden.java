package com.tallerwebi.dominio;

import java.util.List;

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
}
