package com.tallerwebi.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@SuppressWarnings("PMD.TooManyFields")
public class OrdenReparacion {

  private String emailCliente;
  private String accesorios;
  private LocalDateTime fechaIngreso = LocalDateTime.now();
  private LocalDateTime fechaAsignacion;

  private static final double MONTO_TOPE_BASE = 70000.0;
  private static final double PISO_MANO_OBRA_ESTIMADA = 20000.0;
  private static final double FACTOR_MEDIO = 1.35;
  private static final double LIMITE_REPUESTOS_MEDIO = 150000.0;
  private static final double TOPE_MANO_OBRA_ALTA = 60000.0;
  private static final double FACTOR_ALTO = 1.25;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "tecnico_id")
  private Usuario tecnicoAsignado;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idOrdenReparacion;

  private String nombreCliente;
  private String telefonoCliente; // Integer
  // Agregar DNI
  // Agregar mail
  private String modeloEquipo;
  private String descripcionFalla;
  private Integer codigoSeguimiento;
  // private String estado;

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

  public Prioridad calcularPrioridad(LocalDateTime now) {
    final long DIAS_PRIORIDAD_ALTA = 8;
    final long DIAS_PRIORIDAD_MEDIA = 4;

    if (this.fechaIngreso == null) {
      throw new com.tallerwebi.dominio.excepcion.FechaIngresoNoDefinidaException(
        "La orden no tiene fecha de ingreso"
      );
    }
    long dias = ChronoUnit.DAYS.between(this.fechaIngreso, now);
    if (dias >= DIAS_PRIORIDAD_ALTA) {
      return Prioridad.ALTA;
    }
    if (dias >= DIAS_PRIORIDAD_MEDIA) {
      return Prioridad.MEDIA;
    } else {
      return Prioridad.BAJA;
    }
  }

  public Prioridad getPrioridad() {
    return calcularPrioridad(LocalDateTime.now());
  }

  public String getColorPrioridad() {
    if (this.estado == EstadoOrden.ENTREGADO) {
      return "gris";
    }
    switch (getPrioridad()) {
      case ALTA:
        return "rojo";
      case MEDIA:
        return "amarillo";
      default:
        return "verde";
    }
  }

  public Double getCostoManoDeObra() {
    if (this.montoTotal == null || this.montoTotal <= 0.0) {
      return 0.0;
    }

    if (this.montoTotal <= MONTO_TOPE_BASE) {
      return Math.min(PISO_MANO_OBRA_ESTIMADA, this.montoTotal);
    }

    double repuestosEstimadosMedio = this.montoTotal / FACTOR_MEDIO;
    if (repuestosEstimadosMedio <= LIMITE_REPUESTOS_MEDIO) {
      return this.montoTotal - repuestosEstimadosMedio;
    }

    double sinTope = this.montoTotal - (this.montoTotal / FACTOR_ALTO);
    return Math.min(sinTope, TOPE_MANO_OBRA_ALTA);
  }

  public Double getSubtotalRepuestos() {
    if (this.montoTotal == null || this.montoTotal <= 0.0) {
      return 0.0;
    }
    return Math.max(0.0, this.montoTotal - getCostoManoDeObra());
  }
}
