package com.tallerwebi.dominio;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Embeddable
public class ReservaRepuesto {

  @ManyToOne(optional = false)
  @JoinColumn(name = "repuesto_id", nullable = false)
  private Repuesto repuesto;
  private Integer cantidad;

  protected ReservaRepuesto() {}

  public ReservaRepuesto(Repuesto repuesto, Integer cantidad) {
    this.repuesto = repuesto;
    this.cantidad = cantidad;
  }

  public Repuesto getRepuesto() {
    return repuesto;
  }

  public Integer getCantidad() {
    return cantidad;
  }
}
