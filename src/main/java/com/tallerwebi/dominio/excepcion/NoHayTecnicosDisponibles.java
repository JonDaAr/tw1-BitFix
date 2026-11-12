package com.tallerwebi.dominio.excepcion;

public class NoHayTecnicosDisponibles extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public NoHayTecnicosDisponibles(String mensaje) {
    super(mensaje);
  }
}
