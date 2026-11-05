package com.tallerwebi.infraestructura;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HibernateInfraestructuraTestConfig.class)
@Transactional
@Rollback
public class CodigoSeguimientoPersistenciaTest {

  @Autowired
  private SessionFactory factory;

  @Test
  public void laBaseImpideGuardarDosOrdenesConElMismoCodigo() {
    RepositorioOrdenReparacionImpl repositorio = new RepositorioOrdenReparacionImpl(factory);
    OrdenReparacion primera = new OrdenReparacion();
    primera.setCodigoSeguimiento(777777);
    repositorio.guardarOrden(primera);
    assertTrue(repositorio.existeCodigo(777777));
    assertFalse(repositorio.existeCodigo(777778));
    OrdenReparacion segunda = new OrdenReparacion();
    segunda.setCodigoSeguimiento(777777);
    assertThrows(org.hibernate.exception.ConstraintViolationException.class,
      () -> repositorio.guardarOrden(segunda));
  }
}
