package com.tallerwebi.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Importes {

  private Importes() {}

  public static BigDecimal decimal(Double valor) {
    if (valor == null) {
      return null;
    }
    if (!Double.isFinite(valor)) {
      throw new IllegalArgumentException("El importe debe ser finito.");
    }
    return redondear(BigDecimal.valueOf(valor));
  }

  public static BigDecimal redondear(BigDecimal valor) {
    return valor.setScale(2, RoundingMode.HALF_UP);
  }

  public static Double sumar(Double primero, Double segundo) {
    return decimal(primero).add(decimal(segundo)).doubleValue();
  }
}
