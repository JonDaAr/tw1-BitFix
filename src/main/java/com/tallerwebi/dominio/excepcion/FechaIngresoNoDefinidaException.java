package com.tallerwebi.dominio.excepcion;

public class FechaIngresoNoDefinidaException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public FechaIngresoNoDefinidaException(String mensaje) {
    super(mensaje);
  }
}
