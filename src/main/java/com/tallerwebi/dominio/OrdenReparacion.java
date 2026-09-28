package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class OrdenReparacion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idOrdenReparacion;

  private String nombreCliente;
  private String telefonoCliente; //Integer
  //Agregar DNI
  //Agregar mail
  private String modeloEquipo;
  private String descripcionFalla;
  private Integer codigoSeguimiento;

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
}
