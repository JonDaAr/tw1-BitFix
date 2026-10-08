package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorRecepcionTest {

  private ServicioOrdenReparacion servicioOrdenMock;
  private ControladorOrdenReparacion controladorOrdenReparacion;

  @BeforeEach
  public void init() {
    servicioOrdenMock = mock(ServicioOrdenReparacion.class);
    controladorOrdenReparacion = new ControladorOrdenReparacion(servicioOrdenMock);
  }

  @Test
  public void deberiaMostrarFormularioDeRecepcion() {
    when(servicioOrdenMock.listarTodas()).thenReturn(java.util.Collections.emptyList());

    ModelAndView resultado = controladorOrdenReparacion.mostrarRecepcion();

    assertThat(resultado.getViewName(), equalTo("recepcion"));
    assertThat(resultado.getModel().containsKey("datosOrden"), equalTo(true));
    assertThat(resultado.getModel().containsKey("ordenes"), equalTo(true));

    verify(servicioOrdenMock).listarTodas();
  }

  @Test
  public void deberiaRegistrarOrdenYMostrarTecnicoAsignado() throws Exception {
    DatosOrden datos = crearDatos();

    Usuario tecnico = new Usuario();
    tecnico.setEmail("tecnico@test.com");

    OrdenReparacion ordenRegistrada = new OrdenReparacion();
    ordenRegistrada.setTecnicoAsignado(tecnico);
    ordenRegistrada.setCodigoSeguimiento(12345);

    when(servicioOrdenMock.registrarOrden(any(OrdenReparacion.class))).thenReturn(ordenRegistrada);

    when(servicioOrdenMock.obtenerOrdenesParaTecnico())
      .thenReturn(java.util.Collections.emptyList());

    ModelAndView resultado = controladorOrdenReparacion.registrarOrdenDesdeRecepcion(datos);

    assertThat(resultado.getViewName(), equalTo("panel-tecnico"));

    assertThat(resultado.getModel().get("orden"), equalTo(ordenRegistrada));

    assertThat(resultado.getModel().get("codigoSeguimiento"), equalTo(12345));

    assertThat(resultado.getModel().get("mostrarConfirmacion"), equalTo(true));

    assertThat(resultado.getModel().get("vista"), equalTo("nueva-orden"));

    assertThat(
      resultado.getModel().get("datosOrden"),
      org.hamcrest.Matchers.instanceOf(DatosOrden.class)
    );

    verify(servicioOrdenMock).registrarOrden(any(OrdenReparacion.class));
    verify(servicioOrdenMock).obtenerOrdenesParaTecnico();
  }

  @Test
  public void deberiaVolverAlFormularioSiNoHayTecnicosDisponibles() throws Exception {
    DatosOrden datos = crearDatos();

    when(servicioOrdenMock.registrarOrden(any(OrdenReparacion.class)))
      .thenThrow(new NoHayTecnicosDisponibles());

    ModelAndView resultado = controladorOrdenReparacion.registrarOrdenDesdeRecepcion(datos);

    assertThat(resultado.getViewName(), equalTo("panel-tecnico"));

    assertThat(
      resultado.getModel().get("error"),
      equalTo("No hay técnicos disponibles para asignar la orden")
    );

    assertThat(resultado.getModel().get("datosOrden"), equalTo(datos));

    assertThat(resultado.getModel().get("vista"), equalTo("nueva-orden"));
  }

  @Test
  public void deberiaVolverAlFormularioSiLosDatosEstanIncompletos() throws Exception {
    DatosOrden datos = crearDatos();

    when(servicioOrdenMock.registrarOrden(any(OrdenReparacion.class)))
      .thenThrow(new DatosIncompletosException());

    ModelAndView resultado = controladorOrdenReparacion.registrarOrdenDesdeRecepcion(datos);

    assertThat(resultado.getViewName(), equalTo("panel-tecnico"));

    assertThat(
      resultado.getModel().get("error"),
      equalTo("Por favor, complete todos los campos obligatorios.")
    );

    assertThat(resultado.getModel().get("datosOrden"), equalTo(datos));

    assertThat(resultado.getModel().get("vista"), equalTo("nueva-orden"));
  }

  private DatosOrden crearDatos() {
    DatosOrden datos = new DatosOrden();

    datos.setNombreCliente("Juan Perez");
    datos.setEmailCliente("juan@test.com");
    datos.setModeloEquipo("Notebook Dell");
    datos.setDescripcionFalla("No enciende");
    datos.setAccesorios("Cargador");

    return datos;
  }
}
