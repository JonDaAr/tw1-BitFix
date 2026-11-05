package com.tallerwebi.presentacion;

import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = {
  ControladorPresupuesto.class, ControladorOrdenReparacion.class, ControladorInventario.class
})
public class ControladorConflictos {

  @ExceptionHandler({ OptimisticLockingFailureException.class, OptimisticLockException.class })
  public ResponseEntity<String> informarConflicto() {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(
      "La orden o el inventario cambió mientras se procesaba la solicitud. " +
      "Recargá la página y revisá los datos antes de volver a intentarlo."
    );
  }
}
