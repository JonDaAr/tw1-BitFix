package com.tallerwebi.presentacion;

public class DatosCotizacionOnline {

  private String nombreCliente;
  private String emailCliente;
  private String telefonoCliente;
  private String modeloEquipo;
  private String descripcionFalla;

  // --- NUEVOS ATRIBUTOS LOGÍSTICA ---
  private Boolean solicitaRetiro;
  private String direccionCliente;
  private Double latitudCliente;
  private Double longitudCliente;
  private Double costoLogistica;

  public DatosCotizacionOnline() {}

  public String getNombreCliente() {
    return nombreCliente;
  }

  public void setNombreCliente(String nombreCliente) {
    this.nombreCliente = nombreCliente;
  }

  public String getEmailCliente() {
    return emailCliente;
  }

  public void setEmailCliente(String emailCliente) {
    this.emailCliente = emailCliente;
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

  // --- GETTERS Y SETTERS LOGÍSTICA ---
  public Boolean getSolicitaRetiro() {
    return solicitaRetiro;
  }

  public void setSolicitaRetiro(Boolean solicitaRetiro) {
    this.solicitaRetiro = solicitaRetiro;
  }

  public String getDireccionCliente() {
    return direccionCliente;
  }

  public void setDireccionCliente(String direccionCliente) {
    this.direccionCliente = direccionCliente;
  }

  public Double getLatitudCliente() {
    return latitudCliente;
  }

  public void setLatitudCliente(Double latitudCliente) {
    this.latitudCliente = latitudCliente;
  }

  public Double getLongitudCliente() {
    return longitudCliente;
  }

  public void setLongitudCliente(Double longitudCliente) {
    this.longitudCliente = longitudCliente;
  }

  public Double getCostoLogistica() {
    return costoLogistica;
  }

  public void setCostoLogistica(Double costoLogistica) {
    this.costoLogistica = costoLogistica;
  }
}
