package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class OrdenReparacion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idOrdenReparacion;

  private String nombreCliente;
  private String telefonoCliente;
  private String modeloEquipo;
  private String descripcionFalla;
  private Integer codigoSeguimiento;

  // Atributos para Cierre de Orden y Presupuesto
  private String estado = "RECIBIDO";
  private Double montoTotal;
  private LocalDateTime fechaEntrega;
  private String notaTecnica;

  public OrdenReparacion() {}

  public OrdenReparacion(
    String nombreCliente,
    String telefonoCliente,
    String modeloEquipo,
    String descripcionFalla
  ) {
    this.nombreCliente = nombreCliente;
    this.telefonoCliente = telefonoCliente;
    this.modeloEquipo = modeloEquipo;
    this.descripcionFalla = descripcionFalla;
  }

  public Integer generarCodigoSeguimientoUnico() {
    this.codigoSeguimiento = (int) (Math.random() * 900000) + 100000;
    return this.codigoSeguimiento;
  }

  public Long getIdOrdenReparacion() {
    return this.idOrdenReparacion;
  }

  public void setIdOrdenReparacion(Long idOrdenReparacion) {
    this.idOrdenReparacion = idOrdenReparacion;
  }

  public String getNombreCliente() {
    return nombreCliente;
  }

  public void setNombreCliente(String nombreCliente) {
    this.nombreCliente = nombreCliente;
  }

  public String getTelefonoCliente() {
    return telefonoCliente;
  }

  public void setTelefonoCliente(String telefonoCliente) {
    this.telefonoCliente = telefonoCliente;
  }

  public String getModeloEquipo() {
    return modeloEquipo;
  }

  public void setModeloEquipo(String modeloEquipo) {
    this.modeloEquipo = modeloEquipo;
  }

  public String getDescripcionFalla() {
    return descripcionFalla;
  }

  public void setDescripcionFalla(String descripcionFalla) {
    this.descripcionFalla = descripcionFalla;
  }

  public Integer getCodigoSeguimiento() {
    return this.codigoSeguimiento;
  }

  public void setCodigoSeguimiento(Integer codigoSeguimiento) {
    this.codigoSeguimiento = codigoSeguimiento;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public Double getMontoTotal() {
    return montoTotal;
  }

  public void setMontoTotal(Double montoTotal) {
    this.montoTotal = montoTotal;
  }

  public LocalDateTime getFechaEntrega() {
    return fechaEntrega;
  }

  public void setFechaEntrega(LocalDateTime fechaEntrega) {
    this.fechaEntrega = fechaEntrega;
  }

  public String getNotaTecnica() {
    return notaTecnica;
  }

  public void setNotaTecnica(String notaTecnica) {
    this.notaTecnica = notaTecnica;
  }
}
