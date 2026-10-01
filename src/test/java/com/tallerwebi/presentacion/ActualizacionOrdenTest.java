package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.EstadoOrden;
import org.junit.jupiter.api.Test;

public class ActualizacionOrdenTest {

  @Test
  public void deberiaCrearActualizacionOrdenCompleta() {
    Long id = 1L;
    ActualizacionOrden actualizacion = new ActualizacionOrden(
      id,
      "Notebook",
      EstadoOrden.EN_DIAGNOSTICO,
      "Cambio de disco"
    );

    assertEquals(id, actualizacion.getIdOrdenReparacion());
    assertEquals("Notebook", actualizacion.getModeloEquipo());
    assertEquals(EstadoOrden.EN_DIAGNOSTICO, actualizacion.getEstado());
    assertEquals("Cambio de disco", actualizacion.getNotaTecnica());
  }

  @Test
  public void deberiaModificarTodosLosCampos() {
    ActualizacionOrden actualizacion = new ActualizacionOrden();

    actualizacion.setIdOrdenReparacion(5L);
    actualizacion.setModeloEquipo("PC");
    actualizacion.setEstado(EstadoOrden.EN_DIAGNOSTICO);
    actualizacion.setNotaTecnica("Actualizado");

    assertEquals(Long.valueOf(5), actualizacion.getIdOrdenReparacion());
    assertEquals("PC", actualizacion.getModeloEquipo());
    assertEquals(EstadoOrden.EN_DIAGNOSTICO, actualizacion.getEstado());
    assertEquals("Actualizado", actualizacion.getNotaTecnica());
  }
}
