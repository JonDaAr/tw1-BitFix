package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Repuesto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private Double precio;
  private Integer stock;

  public Repuesto() {}

  public Repuesto(String nombre, Double precio, Integer stock) {
    this.nombre = nombre;
    this.precio = precio;
    this.stock = stock;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Double getPrecio() {
    return precio;
  }

  public void setPrecio(Double precio) {
    this.precio = precio;
  }

  public Integer getStock() {
    return stock;
  }

  public void setStock(Integer stock) {
    this.stock = stock;
  }

  public boolean tieneStockSuficiente(int cantidad) {
    return this.stock != null && this.stock >= cantidad;
  }

  public void descontarStock(int cantidad) {
    if (!tieneStockSuficiente(cantidad)) {
      throw new IllegalArgumentException("No hay stock suficiente para descontar");
    }
    this.stock -= cantidad;
  }
}
