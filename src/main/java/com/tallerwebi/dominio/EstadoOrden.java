package com.tallerwebi.dominio;

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
  PENDIENTE_TRIAJE("PENDIENTE COTIZACION"),

  // Nuevo Flujo Cotizacion Online (Previo al ingreso al taller)
  SOLICITUD_COTIZACION("SOLICITUD DE COTIZACION ONLINE"),
  COTIZACION_PREVIA_ENVIADA("COTIZACION PREVIA ENVIADA"),
  ESPERANDO_INGRESO("ESPERANDO INGRESO"),
  COTIZACION_DESCARTADA("COTIZACION DESCARTADA"),
  PENDIENTE_PRESUPUESTO("PENDIENTE PRESUPUESTO");

  private final String descripcion;

  EstadoOrden(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}
