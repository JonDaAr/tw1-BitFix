package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Alerta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String mensaje;

  private String urlAccion;

  private LocalDateTime fechaCreacion = LocalDateTime.now();

  public Alerta() {}

  public Alerta(String mensaje) {
    this.mensaje = mensaje;
    this.fechaCreacion = LocalDateTime.now();
  }

  // Getters y Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getMensaje() {
    return mensaje;
  }

  public void setMensaje(String mensaje) {
    this.mensaje = mensaje;
  }

  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }

  public void setFechaCreacion(LocalDateTime fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }

  public void setUrlAccion(String urlAccion) {
    this.urlAccion = urlAccion;
  }

  public String getUrlAccion() {
    return this.urlAccion;
  }
}
