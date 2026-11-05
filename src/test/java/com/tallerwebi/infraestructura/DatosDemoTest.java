package com.tallerwebi.infraestructura;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.ServicioOrdenReparacionImpl;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HibernateInfraestructuraTestConfig.class)
@ActiveProfiles("datos-demo-test")
public class DatosDemoTest {

  @Autowired
  private DataSource datasource;
  @Autowired
  private SessionFactory factory;
  @Autowired
  private PlatformTransactionManager transactionManager;

  @Test
  public void cargaDemoEsCoherenteYNoRestauraCambiosAlReiniciar() {
    JdbcTemplate jdbc = new JdbcTemplate(datasource);
    jdbc.execute("SET DATABASE SQL SYNTAX MYS TRUE");
    new ResourceDatabasePopulator(new FileSystemResource("src/main/resources/data.sql")).execute(datasource);
    ResourceDatabasePopulator demo = new ResourceDatabasePopulator(
      new FileSystemResource("src/main/resources/demo.sql"));
    demo.execute(datasource);
    assertEquals(8, contar(jdbc, "Repuesto"));
    assertEquals(10, contar(jdbc, "OrdenReparacion"));
    assertEquals(4, contar(jdbc, "reserva_presupuesto"));
    assertEquals(1, contar(jdbc, "carga_demo"));
    assertEquals(0, jdbc.queryForObject("select count(*) from OrdenReparacion orden join Usuario tecnico " +
      "on tecnico.id=orden.tecnico_id where orden.nivel_reparacion <> tecnico.nivel_soporte", Integer.class));
    assertEquals(0, jdbc.queryForObject("select count(*) from OrdenReparacion where " +
      "montoTotal <> subtotalPresupuestado + manoDeObraPresupuestada", Integer.class));
    assertEquals(0, jdbc.queryForObject("select count(*) from OrdenReparacion orden where " +
      "orden.subtotalPresupuestado <> (select coalesce(sum(reserva.cantidad * reserva.precioUnitario),0) " +
      "from reserva_presupuesto reserva where reserva.orden_id=orden.idOrdenReparacion)", Integer.class));
    assertEquals(1, jdbc.queryForObject("select count(*) from OrdenReparacion " +
      "where estado='ENTREGADO' and fechaEntrega > fechaIngreso", Integer.class));
    demo.execute(datasource);
    assertEquals(10, contar(jdbc, "OrdenReparacion"));
    assertEquals(4, contar(jdbc, "reserva_presupuesto"));

    ServicioOrdenReparacionImpl servicio = new ServicioOrdenReparacionImpl(
      new RepositorioUsuarioImpl(factory), new RepositorioOrdenReparacionImpl(factory));
    new TransactionTemplate(transactionManager).executeWithoutResult(estado ->
      servicio.rechazarPresupuesto(910004, "cliente@bitfix.com"));
    assertEquals(9, jdbc.queryForObject("select stock from Repuesto where nombre='DEMO - Memoria RAM DDR4 8GB'", Integer.class));
    jdbc.update("update Repuesto set stock=2, precio=9999 where nombre='DEMO - Cable SATA'");
    jdbc.update("delete from OrdenReparacion where codigoSeguimiento=910001");
    demo.execute(datasource);
    assertEquals(9, contar(jdbc, "OrdenReparacion"));
    assertEquals("PRESUPUESTO_RECHAZADO", jdbc.queryForObject("select estado from OrdenReparacion " +
      "where codigoSeguimiento=910004", String.class));
    assertEquals(9, jdbc.queryForObject("select stock from Repuesto where nombre='DEMO - Memoria RAM DDR4 8GB'", Integer.class));
    assertEquals(2, jdbc.queryForObject("select stock from Repuesto where nombre='DEMO - Cable SATA'", Integer.class));
  }

  private int contar(JdbcTemplate jdbc, String tabla) {
    return jdbc.queryForObject("select count(*) from " + tabla, Integer.class);
  }
}
