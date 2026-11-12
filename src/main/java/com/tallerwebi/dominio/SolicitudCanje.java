package com.tallerwebi.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class SolicitudCanje {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "cliente_id", nullable = false)
  private Usuario cliente;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "computadora_id", nullable = false)
  private ComputadoraUsada computadora;

  @Column(nullable = false, length = 100)
  private String equipoMarca;

  @Column(nullable = false, length = 120)
  private String equipoModelo;

  @Column(nullable = false, length = 1000)
  private String equipoDescripcion;

  private Double valorOfrecido;
  private Double valorAprobado;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoSolicitudCanje estado = EstadoSolicitudCanje.PENDIENTE;

  @Column(nullable = false)
  private LocalDateTime fechaSolicitud = LocalDateTime.now();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Usuario getCliente() {
    return cliente;
  }

  public void setCliente(Usuario cliente) {
    this.cliente = cliente;
  }

  public ComputadoraUsada getComputadora() {
    return computadora;
  }

  public void setComputadora(ComputadoraUsada computadora) {
    this.computadora = computadora;
  }

  public String getEquipoMarca() {
    return equipoMarca;
  }

  public void setEquipoMarca(String equipoMarca) {
    this.equipoMarca = equipoMarca;
  }

  public String getEquipoModelo() {
    return equipoModelo;
  }

  public void setEquipoModelo(String equipoModelo) {
    this.equipoModelo = equipoModelo;
  }

  public String getEquipoDescripcion() {
    return equipoDescripcion;
  }

  public void setEquipoDescripcion(String equipoDescripcion) {
    this.equipoDescripcion = equipoDescripcion;
  }

  public Double getValorOfrecido() {
    return valorOfrecido;
  }

  public void setValorOfrecido(Double valorOfrecido) {
    this.valorOfrecido = valorOfrecido;
  }

  public Double getValorAprobado() {
    return valorAprobado;
  }

  public void setValorAprobado(Double valorAprobado) {
    this.valorAprobado = valorAprobado;
  }

  public EstadoSolicitudCanje getEstado() {
    return estado;
  }

  public void setEstado(EstadoSolicitudCanje estado) {
    this.estado = estado;
  }

  public LocalDateTime getFechaSolicitud() {
    return fechaSolicitud;
  }

  public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
    this.fechaSolicitud = fechaSolicitud;
  }
}
