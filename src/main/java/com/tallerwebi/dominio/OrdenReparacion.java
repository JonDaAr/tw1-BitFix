package com.tallerwebi.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@SuppressWarnings("PMD.TooManyFields")
public class OrdenReparacion {

  private static final SecureRandom GENERADOR_CODIGOS = new SecureRandom();

  @Version
  private long version;

  @ElementCollection
  @CollectionTable(name = "reserva_presupuesto", joinColumns = @JoinColumn(name = "orden_id"))
  private List<ReservaRepuesto> reservasPresupuesto = new ArrayList<>();

  public List<ReservaRepuesto> getReservasPresupuesto() {
    return reservasPresupuesto;
  }

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
  @Column(unique = true)
  private Integer codigoSeguimiento;
  // private String estado;

  // Atributos para Cierre de Orden y Presupuesto

  @Column(precision = 19, scale = 2)
  private BigDecimal montoTotal;
  @Column(precision = 19, scale = 2)
  private BigDecimal subtotalPresupuestado;
  @Column(precision = 19, scale = 2)
  private BigDecimal manoDeObraPresupuestada;
  private LocalDateTime fechaEntrega;
  private String notaTecnica;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", length = 50)
  private EstadoOrden estado;

  @Enumerated(EnumType.STRING)
  @Column(name = "nivel_reparacion", length = 30)
  private NivelReparacion nivelReparacion = NivelReparacion.NIVEL_3;

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
    this.codigoSeguimiento = GENERADOR_CODIGOS.nextInt(900000) + 100000;
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

  public boolean estaActiva() {
    return estado != null && estado.esActivo();
  }

  public void setEstado(EstadoOrden estado) {
    this.estado = estado;
  }

  public NivelReparacion getNivelReparacion() {
    return this.nivelReparacion;
  }

  public void setNivelReparacion(NivelReparacion nivelReparacion) {
    this.nivelReparacion = (nivelReparacion != null) ? nivelReparacion : NivelReparacion.NIVEL_3;
  }

  public Double getMontoTotal() {
    return montoTotal == null ? null : montoTotal.doubleValue();
  }

  public void setMontoTotal(Double montoTotal) {
    this.montoTotal = Importes.decimal(montoTotal);
  }

  public void registrarImportesPresupuesto(BigDecimal subtotal, BigDecimal manoDeObra) {
    this.subtotalPresupuestado = Importes.redondear(subtotal);
    this.manoDeObraPresupuestada = Importes.redondear(manoDeObra);
    this.montoTotal = this.subtotalPresupuestado.add(this.manoDeObraPresupuestada);
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
    if (manoDeObraPresupuestada != null) {
      return manoDeObraPresupuestada.doubleValue();
    }
    if (this.montoTotal == null || this.montoTotal.signum() <= 0) {
      return 0.0;
    }

    if (this.montoTotal.compareTo(BigDecimal.valueOf(MONTO_TOPE_BASE)) <= 0) {
      return this.montoTotal.min(BigDecimal.valueOf(PISO_MANO_OBRA_ESTIMADA)).doubleValue();
    }

    BigDecimal repuestosEstimadosMedio = this.montoTotal.divide(BigDecimal.valueOf(FACTOR_MEDIO), 2, RoundingMode.HALF_UP);
    if (repuestosEstimadosMedio.compareTo(BigDecimal.valueOf(LIMITE_REPUESTOS_MEDIO)) <= 0) {
      return this.montoTotal.subtract(repuestosEstimadosMedio).doubleValue();
    }

    BigDecimal repuestos = this.montoTotal.divide(BigDecimal.valueOf(FACTOR_ALTO), 2, RoundingMode.HALF_UP);
    return this.montoTotal.subtract(repuestos).min(BigDecimal.valueOf(TOPE_MANO_OBRA_ALTA)).doubleValue();
  }

  public Double getSubtotalRepuestos() {
    if (subtotalPresupuestado != null) {
      return subtotalPresupuestado.doubleValue();
    }
    if (this.montoTotal == null || this.montoTotal.signum() <= 0) {
      return 0.0;
    }
    return this.montoTotal.subtract(Importes.decimal(getCostoManoDeObra())).max(BigDecimal.ZERO).doubleValue();
  }
}
