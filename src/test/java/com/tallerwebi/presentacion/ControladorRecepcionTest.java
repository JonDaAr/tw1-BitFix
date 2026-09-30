package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
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
    ModelAndView resultado = controladorOrdenReparacion.mostrarRecepcion();

    assertThat(resultado.getViewName(), equalToIgnoringCase("recepcion"));
    assertThat(resultado.getModel().containsKey("datosOrden"), equalTo(true));
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

    ModelAndView resultado = controladorOrdenReparacion.registrarOrdenDesdeRecepcion(datos);

    assertThat(resultado.getViewName(), equalToIgnoringCase("confirmacion-nueva-orden-reparacion"));
    assertThat(resultado.getModel().get("orden"), equalTo(ordenRegistrada));
    verify(servicioOrdenMock).registrarOrden(any(OrdenReparacion.class));
  }

  @Test
  public void deberiaVolverAlFormularioSiNoHayTecnicosDisponibles() throws Exception {
    DatosOrden datos = crearDatos();
    when(servicioOrdenMock.registrarOrden(any(OrdenReparacion.class)))
      .thenThrow(new NoHayTecnicosDisponibles());

    ModelAndView resultado = controladorOrdenReparacion.registrarOrdenDesdeRecepcion(datos);

    assertThat(resultado.getViewName(), equalToIgnoringCase("recepcion"));
    assertThat(
      resultado.getModel().get("error"),
      equalTo("No hay técnicos disponibles para asignar la orden")
    );
    assertThat(resultado.getModel().get("datosOrden"), equalTo(datos));
  }

  @Test
  public void deberiaVolverAlFormularioSiLosDatosEstanIncompletos() throws Exception {
    DatosOrden datos = crearDatos();
    when(servicioOrdenMock.registrarOrden(any(OrdenReparacion.class)))
      .thenThrow(new DatosIncompletosException());

    ModelAndView resultado = controladorOrdenReparacion.registrarOrdenDesdeRecepcion(datos);

    assertThat(resultado.getViewName(), equalToIgnoringCase("recepcion"));
    assertThat(
      resultado.getModel().get("error"),
      equalTo("Por favor, complete todos los campos obligatorios.")
    );
    assertThat(resultado.getModel().get("datosOrden"), equalTo(datos));
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
