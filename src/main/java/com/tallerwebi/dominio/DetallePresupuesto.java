package com.tallerwebi.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class DetallePresupuesto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "orden_id", nullable = false)
  private OrdenReparacion ordenReparacion;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "repuesto_id", nullable = false)
  private Repuesto repuesto;

  @Column(nullable = false)
  private Integer cantidad;

  @Column(nullable = false)
  private boolean stockDevuelto;

  public DetallePresupuesto() {}

  public DetallePresupuesto(OrdenReparacion ordenReparacion, Repuesto repuesto, Integer cantidad) {
    this.ordenReparacion = ordenReparacion;
    this.repuesto = repuesto;
    this.cantidad = cantidad;
  }

  public Long getId() {
    return id;
  }

  public OrdenReparacion getOrdenReparacion() {
    return ordenReparacion;
  }

  public void setOrdenReparacion(OrdenReparacion ordenReparacion) {
    this.ordenReparacion = ordenReparacion;
  }

  public Repuesto getRepuesto() {
    return repuesto;
  }

  public void setRepuesto(Repuesto repuesto) {
    this.repuesto = repuesto;
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }

  public boolean isStockDevuelto() {
    return stockDevuelto;
  }

  public void setStockDevuelto(boolean stockDevuelto) {
    this.stockDevuelto = stockDevuelto;
  }
}
