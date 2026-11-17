package com.tallerwebi.dominio;

public class ResultadoCotizacionLogistica {

  private Sucursal sucursalAsignada;
  private Double distanciaKm;
  private Double costoTotal;
  private boolean viable;
  private String mensajeError;

  public ResultadoCotizacionLogistica() {}

  // Getters y Setters
  public Sucursal getSucursalAsignada() {
    return sucursalAsignada;
  }

  public void setSucursalAsignada(Sucursal sucursalAsignada) {
    this.sucursalAsignada = sucursalAsignada;
  }

  public Double getDistanciaKm() {
    return distanciaKm;
  }

  public void setDistanciaKm(Double distanciaKm) {
    this.distanciaKm = distanciaKm;
  }

  public Double getCostoTotal() {
    return costoTotal;
  }

  public void setCostoTotal(Double costoTotal) {
    this.costoTotal = costoTotal;
  }

  public boolean isViable() {
    return viable;
  }

  public void setViable(boolean viable) {
    this.viable = viable;
  }

  public String getMensajeError() {
    return mensajeError;
  }

  public void setMensajeError(String mensajeError) {
    this.mensajeError = mensajeError;
  }
}
