package com.tallerwebi.dominio;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import java.math.BigDecimal;

@Embeddable
public class ReservaRepuesto {

  @ManyToOne(optional = false)
  @JoinColumn(name = "repuesto_id", nullable = false)
  private Repuesto repuesto;
  private Integer cantidad;
  private String nombreRepuesto;
  @Column(precision = 19, scale = 2)
  private BigDecimal precioUnitario;

  protected ReservaRepuesto() {}

  public ReservaRepuesto(Repuesto repuesto, Integer cantidad) {
    this.repuesto = repuesto;
    this.cantidad = cantidad;
    this.nombreRepuesto = repuesto.getNombre();
    this.precioUnitario = Importes.decimal(repuesto.getPrecio());
  }

  public Repuesto getRepuesto() {
    return repuesto;
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public String getNombreRepuesto() {
    return nombreRepuesto;
  }

  public BigDecimal getPrecioUnitario() {
    return precioUnitario;
  }

  public BigDecimal getSubtotal() {
    return precioUnitario == null ? null : precioUnitario.multiply(BigDecimal.valueOf(cantidad));
  }
}
