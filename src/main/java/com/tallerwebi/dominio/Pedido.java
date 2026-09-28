package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Pedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String codigoSeguimiento;
  private String estado;
  private String descripcion;

  public Pedido() {}

  public Pedido(String codigoSeguimiento, String estado) {
    this.codigoSeguimiento = codigoSeguimiento;
    this.estado = estado;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCodigoSeguimiento() {
    return codigoSeguimiento;
  }

  public void setCodigoSeguimiento(String codigoSeguimiento) {
    this.codigoSeguimiento = codigoSeguimiento;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }
}
