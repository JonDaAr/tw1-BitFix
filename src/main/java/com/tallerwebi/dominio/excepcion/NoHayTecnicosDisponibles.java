package com.tallerwebi.dominio.excepcion;

public class NoHayTecnicosDisponibles extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public NoHayTecnicosDisponibles(String s) {
    super("No hay tecnicos disponibles para asignar la orden");
  }
}
