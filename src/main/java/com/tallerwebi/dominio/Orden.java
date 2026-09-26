package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class Orden {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombreCliente;
  private String emailCliente;
  private String tipoEquipo;
  private String fallaReportada;
  private String accesorios;

  private String estado = "RECIBIDO";

  private LocalDateTime fechaIngreso = LocalDateTime.now();

  private LocalDateTime fechaAsignacion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tecnico_id")
  private Usuario tecnicoAsignado;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public LocalDateTime getFechaIngreso() {
    return fechaIngreso;
  }

  public void setFechaIngreso(LocalDateTime fechaIngreso) {
    this.fechaIngreso = fechaIngreso;
  }

  public LocalDateTime getFechaAsignacion() {
    return fechaAsignacion;
  }

  public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
    this.fechaAsignacion = fechaAsignacion;
  }

  public Usuario getTecnicoAsignado() {
    return tecnicoAsignado;
  }

  public void setTecnicoAsignado(Usuario tecnicoAsignado) {
    this.tecnicoAsignado = tecnicoAsignado;
  }
}
