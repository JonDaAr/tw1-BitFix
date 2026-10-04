package com.tallerwebi.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@SuppressWarnings("PMD.TooManyFields")
public class OrdenReparacion {

  private String emailCliente;
  private String accesorios;
  private LocalDateTime fechaIngreso = LocalDateTime.now();
  private LocalDateTime fechaAsignacion;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "tecnico_id")
  private Usuario tecnicoAsignado;

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
  //private String estado;

  // Atributos para Cierre de Orden y Presupuesto

  private Double montoTotal;
  private LocalDateTime fechaEntrega;
  private String notaTecnica;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", length = 50)
  private EstadoOrden estado;

  public OrdenReparacion() {
    this.estado = EstadoOrden.RECIBIDO;
  }

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
    this.estado = EstadoOrden.RECIBIDO;
    this.notaTecnica = "";
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

  public EstadoOrden getEstado() {
    return estado;
  }

  public void setEstado(EstadoOrden estado) {
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

  public Usuario getTecnicoAsignado() {
    return tecnicoAsignado;
  }

  public void setTecnicoAsignado(Usuario tecnicoAsignado) {
    this.tecnicoAsignado = tecnicoAsignado;
  }

  public LocalDateTime getFechaAsignacion() {
    return fechaAsignacion;
  }

  public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
    this.fechaAsignacion = fechaAsignacion;
  }

  public String getEmailCliente() {
    return emailCliente;
  }

  public void setEmailCliente(String emailCliente) {
    this.emailCliente = emailCliente;
  }

  public String getAccesorios() {
    return accesorios;
  }

  public void setAccesorios(String accesorios) {
    this.accesorios = accesorios;
  }

  public LocalDateTime getFechaIngreso() {
    return fechaIngreso;
  }

  public void setFechaIngreso(LocalDateTime fechaIngreso) {
    this.fechaIngreso = fechaIngreso;
  }
}
