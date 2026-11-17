package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.EstadoOrden;
import com.tallerwebi.dominio.NivelReparacion;

public class DatosOrden {

  private Integer codigoSeguimiento;
  private String nombreCliente;
  private String telefonoCliente;
  private String modeloEquipo;
  private String descripcionFalla;
  private String emailCliente;
  private String accesorios;
  private EstadoOrden estado;
  private NivelReparacion nivelReparacion;

  // NUEVO: Para transportar la contraseña generada a la vista
  private String passwordProvisoria;

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
    this.estado = EstadoOrden.ENTREGADO;
  }

  // ... (Tus getters y setters originales quedan igual) ...

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

  public String getEmailCliente() {
    return emailCliente;
  }

  public void setEmailCliente(String emailCliente) {
    this.emailCliente = emailCliente;
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

  public String getAccesorios() {
    return accesorios;
  }

  public void setAccesorios(String accesorios) {
    this.accesorios = accesorios;
  }

  public EstadoOrden getEstado() {
    return estado;
  }

  public void setEstado(EstadoOrden estado) {
    this.estado = estado;
  }

  public Integer getCodigoSeguimiento() {
    return codigoSeguimiento;
  }

  public void setCodigoSeguimiento(Integer codigoSeguimiento) {
    this.codigoSeguimiento = codigoSeguimiento;
  }

  public NivelReparacion getNivelReparacion() {
    return nivelReparacion;
  }

  public void setNivelReparacion(NivelReparacion nivelReparacion) {
    this.nivelReparacion = nivelReparacion;
  }

  // NUEVOS Getters y Setters
  public String getPasswordProvisoria() {
    return passwordProvisoria;
  }

  public void setPasswordProvisoria(String passwordProvisoria) {
    this.passwordProvisoria = passwordProvisoria;
  }
}
