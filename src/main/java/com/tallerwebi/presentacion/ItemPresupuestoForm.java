package com.tallerwebi.presentacion;

import java.io.Serializable;

public class ItemPresupuestoForm implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long repuestoId;
  private Integer cantidad;

  public ItemPresupuestoForm() {}

  public ItemPresupuestoForm(Long repuestoId, Integer cantidad) {
    this.repuestoId = repuestoId;
    this.cantidad = cantidad;
  }

  public Long getRepuestoId() {
    return repuestoId;
  }

  public void setRepuestoId(Long repuestoId) {
    this.repuestoId = repuestoId;
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }
}
