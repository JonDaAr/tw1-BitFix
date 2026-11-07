package com.tallerwebi.dominio;

import jakarta.persistence.*;

@Entity
public class Sucursal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String direccion;
  private Double latitud;
  private Double longitud;
  private Boolean activo;

  @Transient
  private Double distancia;

  // Getters y Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDireccion() {
    return direccion;
  }

  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }

  public Double getLatitud() {
    return latitud;
  }

  public void setLatitud(Double latitud) {
    this.latitud = latitud;
  }

  public Double getLongitud() {
    return longitud;
  }

  public void setLongitud(Double longitud) {
    this.longitud = longitud;
  }

  public Boolean getActivo() {
    return activo;
  }

  public void setActivo(Boolean activo) {
    this.activo = activo;
  }

  public Double getDistancia() {
    return distancia;
  }

  public void setDistancia(Double distancia) {
    this.distancia = distancia;
  }
}
