package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.EstadoInvalidoParaCierreException;
import com.tallerwebi.dominio.excepcion.OrdenNoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCierreOrdenTest {

  private RepositorioOrdenReparacion repositorioOrdenReparacionMock;
  private ServicioCierreOrden servicioCierreOrden;

  @BeforeEach
  public void init() {
    repositorioOrdenReparacionMock = mock(RepositorioOrdenReparacion.class);
    servicioCierreOrden = new ServicioCierreOrdenImpl(repositorioOrdenReparacionMock);
  }

  @Test
  public void test01_obtenerOrdenParaComprobante_Exitoso() throws OrdenNoEncontradaException {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(10L);
    when(repositorioOrdenReparacionMock.buscarPorId(10L)).thenReturn(orden);

    OrdenReparacion resultado = servicioCierreOrden.obtenerOrdenParaComprobante(10L);

    assertThat(resultado, is(notNullValue()));
    assertThat(resultado.getIdOrdenReparacion(), is(10L));
  }

  @Test
  public void test02_obtenerOrdenParaComprobante_OrdenNoEncontrada_LanzaExcepcion() {
    when(repositorioOrdenReparacionMock.buscarPorId(99L)).thenReturn(null);

    assertThrows(
      OrdenNoEncontradaException.class,
      () -> {
        servicioCierreOrden.obtenerOrdenParaComprobante(99L);
      }
    );
  }

  @Test
  public void test03_cerrarOrden_OrdenInexistente_LanzaOrdenNoEncontradaException() {
    when(repositorioOrdenReparacionMock.buscarPorId(50L)).thenReturn(null);

    assertThrows(
      OrdenNoEncontradaException.class,
      () -> {
        servicioCierreOrden.cerrarOrden(50L);
      }
    );
  }

  @Test
  public void test04_cerrarOrden_EstadoReparado_CambiaAEntregadoExitosamente()
    throws OrdenNoEncontradaException, EstadoInvalidoParaCierreException {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(1L);
    orden.setEstado("REPARADO");
    orden.setMontoTotal(15000.0);

    when(repositorioOrdenReparacionMock.buscarPorId(1L)).thenReturn(orden);

    OrdenReparacion resultado = servicioCierreOrden.cerrarOrden(1L);

    assertThat(resultado.getEstado(), equalToIgnoringCase("ENTREGADO"));
  }

  @Test
  public void test05_cerrarOrden_EstadoRecibido_LanzaEstadoInvalidoException() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(2L);
    orden.setEstado("RECIBIDO");

    when(repositorioOrdenReparacionMock.buscarPorId(2L)).thenReturn(orden);

    assertThrows(
      EstadoInvalidoParaCierreException.class,
      () -> {
        servicioCierreOrden.cerrarOrden(2L);
      }
    );
  }

  @Test
  public void test06_cerrarOrden_EstadoEnDiagnostico_LanzaEstadoInvalidoException() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(3L);
    orden.setEstado("EN_DIAGNOSTICO");

    when(repositorioOrdenReparacionMock.buscarPorId(3L)).thenReturn(orden);

    assertThrows(
      EstadoInvalidoParaCierreException.class,
      () -> {
        servicioCierreOrden.cerrarOrden(3L);
      }
    );
  }

  @Test
  public void test07_cerrarOrden_EstadoEsperandoRepuesto_LanzaEstadoInvalidoException() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(4L);
    orden.setEstado("ESPERANDO_REPUESTO");

    when(repositorioOrdenReparacionMock.buscarPorId(4L)).thenReturn(orden);

    assertThrows(
      EstadoInvalidoParaCierreException.class,
      () -> {
        servicioCierreOrden.cerrarOrden(4L);
      }
    );
  }

  @Test
  public void test08_cerrarOrden_EstadoYaEntregado_LanzaEstadoInvalidoException() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(5L);
    orden.setEstado("ENTREGADO");

    when(repositorioOrdenReparacionMock.buscarPorId(5L)).thenReturn(orden);

    assertThrows(
      EstadoInvalidoParaCierreException.class,
      () -> {
        servicioCierreOrden.cerrarOrden(5L);
      }
    );
  }

  @Test
  public void test09_cerrarOrden_EstadoNull_LanzaEstadoInvalidoException() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(6L);
    orden.setEstado(null);

    when(repositorioOrdenReparacionMock.buscarPorId(6L)).thenReturn(orden);

    assertThrows(
      EstadoInvalidoParaCierreException.class,
      () -> {
        servicioCierreOrden.cerrarOrden(6L);
      }
    );
  }

  @Test
  public void test10_cerrarOrden_EstableceFechaYHoraDeEntrega()
    throws OrdenNoEncontradaException, EstadoInvalidoParaCierreException {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(7L);
    orden.setEstado("REPARADO");
    orden.setMontoTotal(8000.0);

    when(repositorioOrdenReparacionMock.buscarPorId(7L)).thenReturn(orden);

    OrdenReparacion resultado = servicioCierreOrden.cerrarOrden(7L);

    assertThat(resultado.getFechaEntrega(), is(notNullValue()));
  }

  @Test
  public void test11_cerrarOrden_InvocaMetodoModificarEnRepositorio()
    throws OrdenNoEncontradaException, EstadoInvalidoParaCierreException {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(8L);
    orden.setEstado("REPARADO");
    orden.setMontoTotal(12000.0);

    when(repositorioOrdenReparacionMock.buscarPorId(8L)).thenReturn(orden);

    servicioCierreOrden.cerrarOrden(8L);

    verify(repositorioOrdenReparacionMock, times(1)).modificarOrden(orden);
  }

  @Test
  public void test12_cerrarOrden_ConservaNotaTecnicaYDatosDelComprobante()
    throws OrdenNoEncontradaException, EstadoInvalidoParaCierreException {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(9L);
    orden.setEstado("REPARADO");
    orden.setMontoTotal(5000.0);
    orden.setNotaTecnica("Se cambió pantalla");

    when(repositorioOrdenReparacionMock.buscarPorId(9L)).thenReturn(orden);

    OrdenReparacion resultado = servicioCierreOrden.cerrarOrden(9L);

    assertThat(resultado.getNotaTecnica(), is("Se cambió pantalla"));
  }

  @Test
  public void test13_cerrarOrden_SinMontoTotalOMontoInvalido_LanzaExcepcion() {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(11L);
    orden.setEstado("REPARADO");
    orden.setMontoTotal(0.0);

    when(repositorioOrdenReparacionMock.buscarPorId(11L)).thenReturn(orden);

    assertThrows(
      EstadoInvalidoParaCierreException.class,
      () -> {
        servicioCierreOrden.cerrarOrden(11L);
      }
    );
  }
}
