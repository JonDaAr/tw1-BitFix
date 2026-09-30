package com.tallerwebi.presentacion;

public class DatosOrden {

  private Integer codigoSeguimiento;
  private String nombreCliente;
  private String telefonoCliente;
  private String modeloEquipo;
  private String descripcionFalla;
  private String emailCliente;
  private String accesorios;
  private String estado;

  public DatosOrden() {}

  public DatosOrden(
    String nombreCliente,
    String telefonoCliente,
    String modeloEquipo,
    String descripcionFalla
  ) {
    this.nombreCliente = nombreCliente;
    this.telefonoCliente = telefonoCliente;
    this.modeloEquipo = modeloEquipo;
    this.descripcionFalla = descripcionFalla;
    this.estado = "ENTREGADO";
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

  public String getEmailCliente() {
    return emailCliente;
  }

  public void setEmailCliente(String emailCliente) {
    this.emailCliente = emailCliente;
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

  public String getAccesorios() {
    return accesorios;
  }

  public void setDescripcionFalla(String descripcionFalla) {
    this.descripcionFalla = descripcionFalla;
  }

  public void setAccesorios(String accesorios) {
    this.accesorios = accesorios;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public Integer getCodigoSeguimiento() {
    return codigoSeguimiento;
  }

  public void setCodigoSeguimiento(Integer codigoSeguimiento) {
    this.codigoSeguimiento = codigoSeguimiento;
  }
}
