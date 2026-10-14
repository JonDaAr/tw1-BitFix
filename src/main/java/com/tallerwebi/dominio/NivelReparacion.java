package com.tallerwebi.dominio;

public enum NivelReparacion {
  NIVEL_1("Nivel 1 (Tier 1): Mantenimiento básico, formateo, instalación de software, limpieza"),
  NIVEL_2("Nivel 2 (Tier 2): Cambio de hardware (pantallas, discos, memorias RAM), ensamblaje"),
  NIVEL_3(
    "Nivel 3 (Tier 3): Reparaciones complejas, soldadura de microelectrónica, reparación de placa madre"
  );

  private final String descripcion;

  NivelReparacion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}
