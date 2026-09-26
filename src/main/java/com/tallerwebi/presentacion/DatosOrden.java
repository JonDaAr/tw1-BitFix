package com.tallerwebi.presentacion;

public class DatosOrden {

  private String nombreCliente;
  private String emailCliente;
  private String tipoEquipo;
  private String fallaReportada;
  private String accesorios;

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

  public String getTipoEquipo() {
    return tipoEquipo;
  }

  public void setTipoEquipo(String tipoEquipo) {
    this.tipoEquipo = tipoEquipo;
  }

  public String getFallaReportada() {
    return fallaReportada;
  }

  public void setFallaReportada(String fallaReportada) {
    this.fallaReportada = fallaReportada;
  }

  public String getAccesorios() {
    return accesorios;
  }

  public void setAccesorios(String accesorios) {
    this.accesorios = accesorios;
  }
}
