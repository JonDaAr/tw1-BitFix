package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.EstadoOrden;

public class ActualizacionOrden {

  //datos minimos para la actualizacion
  private Long idOrdenReparacion;
  private String modeloEquipo;
  private EstadoOrden estado;
  private String notaTecnica;

  public ActualizacionOrden() {}

  public ActualizacionOrden(
    Long idOrdenReparacion,
    String modeloEquipo,
    EstadoOrden estado,
    String notaTecnica
  ) {
    this.idOrdenReparacion = idOrdenReparacion;
    this.modeloEquipo = modeloEquipo;
    this.estado = estado;
    this.notaTecnica = notaTecnica;
  }

  public Long getIdOrdenReparacion() {
    return idOrdenReparacion;
  }

  public String getModeloEquipo() {
    return modeloEquipo;
  }

  public void setModeloEquipo(String modeloEquipo) {
    this.modeloEquipo = modeloEquipo;
  }

  public void setIdOrdenReparacion(Long idOrdenReparacion) {
    this.idOrdenReparacion = idOrdenReparacion;
  }

  public EstadoOrden getEstado() {
    return estado;
  }

  public void setEstado(EstadoOrden estado) {
    this.estado = estado;
  }

  public String getNotaTecnica() {
    return notaTecnica;
  }

  public void setNotaTecnica(String notaTecnica) {
    this.notaTecnica = notaTecnica;
  }
}
