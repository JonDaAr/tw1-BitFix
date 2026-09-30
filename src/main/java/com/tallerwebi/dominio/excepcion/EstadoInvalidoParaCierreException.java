package com.tallerwebi.dominio.excepcion;

public class EstadoInvalidoParaCierreException extends Exception {

  private static final long serialVersionUID = 1L;

  public EstadoInvalidoParaCierreException(String mensaje) {
    super(mensaje);
  }
}
