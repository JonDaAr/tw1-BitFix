package com.tallerwebi.presentacion;

import org.springframework.web.multipart.MultipartFile;

@SuppressWarnings("PMD.TooManyFields") // Le decimos a PMD que ignore esta regla acá
public class DatosCotizacionOnline {

  private String nombreCliente;
  private String emailCliente;
  private String telefonoCliente;
  private String modeloEquipo;
  private String descripcionFalla;

  private Boolean solicitaRetiro;
  private String direccionCliente;
  private Double latitudCliente;
  private Double longitudCliente;
  private Double costoLogistica;

  private MultipartFile[] adjuntos;

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

  public MultipartFile[] getAdjuntos() {
    if (this.adjuntos == null) {
      return new MultipartFile[0];
    }
    return this.adjuntos.clone();
  }

  public void setAdjuntos(MultipartFile... adjuntos) {
    if (adjuntos == null) {
      this.adjuntos = new MultipartFile[0];
    } else {
      this.adjuntos = adjuntos.clone();
    }
  }
}
