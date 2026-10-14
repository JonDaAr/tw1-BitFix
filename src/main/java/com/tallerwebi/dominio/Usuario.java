package com.tallerwebi.dominio;

import com.tallerwebi.dominio.NivelReparacion;
import jakarta.persistence.*;

@Entity
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  private String password;
  private String rol;
  private Boolean activo = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "nivel_soporte", length = 30)
  private NivelReparacion nivelSoporte;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRol() {
    return rol;
  }

  public void setRol(String rol) {
    this.rol = rol;
  }

  public Boolean getActivo() {
    return activo;
  }

  public void setActivo(Boolean activo) {
    this.activo = activo;
  }

  public void activar() {
    activo = true;
  }

  public NivelReparacion getNivelSoporte() {
    return nivelSoporte;
  }

  public void setNivelSoporte(NivelReparacion nivelSoporte) {
    this.nivelSoporte = nivelSoporte;
  }
}
