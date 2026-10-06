package com.tallerwebi.dominio.excepcion;

public class FechaEntregaNoDefinidaException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public FechaEntregaNoDefinidaException(String mensaje) {
    super(mensaje);
  }
}
