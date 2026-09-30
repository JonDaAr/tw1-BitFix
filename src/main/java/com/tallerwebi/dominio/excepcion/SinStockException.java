package com.tallerwebi.dominio.excepcion;

public class SinStockException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public SinStockException(String message) {
    super(message);
  }
}
