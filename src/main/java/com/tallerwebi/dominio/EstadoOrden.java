package com.tallerwebi.dominio;

public enum EstadoOrden {
  RECIBIDO("RECIBIDO"),
  EN_DIAGNOSTICO("EN DIAGNOSTICO"),
  ESPERANDO_REPUESTO("ESPERANDO REPUESTO"),
  PRESUPUESTO_ENVIADO("PRESUPUESTO ENVIADO"),
  PRESUPUESTO_ACEPTADO("PRESUPUESTO ACEPTADO"),
  PRESUPUESTO_RECHAZADO("PRESUPUESTO RECHAZADO"),
  REPARADO("REPARADO"),
  ENTREGADO("ENTREGADO"),
  EN_REPARACION("EN_REPARACION");

  private final String descripcion;

  EstadoOrden(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}
