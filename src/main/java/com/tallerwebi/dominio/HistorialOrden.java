package com.tallerwebi.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class HistorialOrden {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long historialOrdenId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "orden_id")
  private OrdenReparacion ordenReparacion;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private EstadoOrden estado;

  private LocalDateTime fechaHora = LocalDateTime.now();

  public HistorialOrden() {}

  public HistorialOrden(OrdenReparacion ordenReparacion, EstadoOrden estado) {
    this.ordenReparacion = ordenReparacion;
    this.estado = estado;
  }

  public Long getHistorialOrdenId() {
    return historialOrdenId;
  }

  public OrdenReparacion getOrdenReparacion() {
    return ordenReparacion;
  }

  public EstadoOrden getEstado() {
    return estado;
  }

  public void setEstado(EstadoOrden estado) {
    this.estado = estado;
  }

  public LocalDateTime getFechaHora() {
    return fechaHora;
  }
}
