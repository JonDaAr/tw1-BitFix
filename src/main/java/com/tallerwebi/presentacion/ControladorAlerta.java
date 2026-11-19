package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Alerta;
import com.tallerwebi.dominio.ServicioAlerta;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alertas")
public class ControladorAlerta {

  private final ServicioAlerta servicioAlerta;

  @Autowired
  public ControladorAlerta(ServicioAlerta servicioAlerta) {
    this.servicioAlerta = servicioAlerta;
  }

  // 1. Endpoint para traer las alertas pendientes (GET)
  @GetMapping(value = "/pendientes", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<Alerta>> obtenerPendientes() {
    List<Alerta> alertas = servicioAlerta.obtenerAlertasPendientes();
    return ResponseEntity.ok(alertas);
  }

  // 2. Endpoint para marcar una alerta como resuelta (POST)
  @PostMapping(value = "/leida/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> marcarComoLeida(@PathVariable("id") Long id) {
    servicioAlerta.marcarComoLeida(id);
    return ResponseEntity.ok().build();
  }
}
