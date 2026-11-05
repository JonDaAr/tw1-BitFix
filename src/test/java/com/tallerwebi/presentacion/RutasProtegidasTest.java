package com.tallerwebi.presentacion;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.config.SpringWebConfig;
import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioCierreOrden;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.ServicioPresupuesto;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.RepositorioUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class RutasProtegidasTest {

  private MockMvc mvc;
  private ServicioCierreOrden servicioCierre;
  private ServicioOrdenReparacion servicioOrden;
  private ServicioPresupuesto servicioPresupuesto;

  @BeforeEach
  public void preparar() {
    servicioCierre = mock(ServicioCierreOrden.class);
    servicioOrden = mock(ServicioOrdenReparacion.class);
    servicioPresupuesto = mock(ServicioPresupuesto.class);
    RegistroInterceptores registro = new RegistroInterceptores();
    new SpringWebConfig(servicioOrden).addInterceptors(registro);
    mvc = MockMvcBuilders.standaloneSetup(
      new ControladorCierreOrden(servicioCierre),
      new ControladorOrdenReparacion(servicioOrden),
      new ControladorPresupuesto(servicioPresupuesto, servicioOrden),
      new ControladorTecnico(servicioOrden, servicioPresupuesto),
      new ControladorAdmin(mock(ServicioLogin.class), servicioOrden)
    ).addInterceptors(registro.obtener())
      .setViewResolvers(new InternalResourceViewResolver("/WEB-INF/views/thymeleaf/", ".html"))
      .build();
  }

  @Test
  public void rutasDeLecturaRequierenSesion() throws Exception {
    for (String ruta : new String[] {
      "/cierre-orden/1", "/comprobante/1", "/recepcion", "/recepcion-home"
    }) {
      mvc.perform(get(ruta)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
    }
    verifyNoInteractions(servicioCierre, servicioOrden);
  }

  @Test
  public void operacionesDeEscrituraRequierenSesion() throws Exception {
    mvc.perform(post("/cerrar-orden").param("idOrden", "1"))
      .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    mvc.perform(post("/recepcion"))
      .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    mvc.perform(post("/recepcion-home"))
      .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    verifyNoInteractions(servicioCierre, servicioOrden);
  }

  @Test
  public void cierreConSesionConservaSuFuncionamiento() throws Exception {
    MockHttpSession sesion = new MockHttpSession();
    Usuario usuario = new Usuario();
    usuario.setRol("TECNICO");
    sesion.setAttribute("USUARIO", usuario);
    OrdenReparacion orden = new OrdenReparacion();
    orden.setIdOrdenReparacion(1L);
    usuario.setId(2L);
    orden.setTecnicoAsignado(usuario);
    when(servicioOrden.buscarPorId(1L)).thenReturn(orden);
    when(servicioCierre.cerrarOrden(1L)).thenReturn(orden);

    mvc.perform(post("/cerrar-orden").session(sesion).param("idOrden", "1"))
      .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/comprobante/1"));
    verify(servicioCierre).cerrarOrden(1L);
  }

  @Test
  public void rutaDePruebaYaNoExiste() throws Exception {
    mvc.perform(get("/crear-orden-prueba")).andExpect(status().isNotFound());
    verifyNoInteractions(servicioCierre, servicioOrden);
  }

  @Test
  public void consultaPublicaSigueDisponible() throws Exception {
    mvc.perform(get("/consultar-estado"))
      .andExpect(status().isOk()).andExpect(view().name("consultar-estado"));
  }

  @Test
  public void consultaPorCodigoFuncionaSinCuentaPorGetYPost() throws Exception {
    prepararOrden();
    mvc.perform(get("/consulta-estado").param("codigoSeguimiento", "123456"))
      .andExpect(status().isOk()).andExpect(view().name("resultado-consulta"))
      .andExpect(model().attributeExists("pedido"));
    mvc.perform(post("/consulta-estado").param("codigoSeguimiento", "123456"))
      .andExpect(status().isOk()).andExpect(view().name("resultado-consulta"))
      .andExpect(model().attributeExists("pedido"));
  }

  @Test
  public void redireccionAlLoginRespetaElContextoYNoCreaSesion() throws Exception {
    mvc.perform(get("/spring/recepcion").contextPath("/spring"))
      .andExpect(redirectedUrl("/spring/login"))
      .andDo(resultado -> assertNull(resultado.getRequest().getSession(false)));
  }

  @Test
  public void soloAdminPuedeIngresarAlPanelDeAdministracion() throws Exception {
    mvc.perform(get("/admin/panel").session(sesion("TECNICO", 2L, "tecnico@test.com")))
      .andExpect(status().isForbidden());
    mvc.perform(get("/admin/panel").session(sesion("ADMIN", 9L, "admin@test.com")))
      .andExpect(status().isOk()).andExpect(view().name("panel-admin"));
  }

  @Test
  public void rolesDesconocidosONulosNoPuedenOperar() throws Exception {
    for (String rol : new String[] { "OTRO", null }) {
      mvc.perform(post("/cerrar-orden").session(sesion(rol, 2L, "usuario@test.com"))
        .param("idOrden", "1")).andExpect(status().isForbidden());
    }
    verifyNoInteractions(servicioOrden, servicioCierre);
  }

  @Test
  public void clientePuedeConsultarSuComprobantePorCodigoYRegistrarSuEquipo() throws Exception {
    prepararOrden();
    MockHttpSession sesion = sesion("CLIENTE", 4L, "cliente@test.com");
    mvc.perform(get("/comprobante").session(sesion).param("codigoSeguimiento", "123456"))
      .andExpect(status().isOk()).andExpect(view().name("comprobante"));
    mvc.perform(get("/recepcion-home").session(sesion))
      .andExpect(status().isOk()).andExpect(view().name("recepcion-home"));
  }

  @Test
  public void identificadoresRepetidosNoPermitenAutorizarUnaOrdenYModificarOtra() throws Exception {
    MockHttpSession sesion = sesion("TECNICO", 2L, "tecnico@test.com");
    mvc.perform(post("/ordenes/actualizar").session(sesion)
      .param("idOrdenReparacion", "1", "99").param("estado", "ENTREGADO"))
      .andExpect(status().isBadRequest());
    mvc.perform(post("/presupuesto/enviar").session(sesion)
      .param("codigoSeguimiento", "123456", "999999"))
      .andExpect(status().isBadRequest());
    verifyNoInteractions(servicioOrden, servicioPresupuesto);
  }

  @Test
  public void presupuestoSinOrdenPermiteCalcularPeroNoEnviar() throws Exception {
    MockHttpSession sesion = sesion("TECNICO", 2L, "tecnico@test.com");
    mvc.perform(get("/presupuesto").session(sesion))
      .andExpect(status().isOk()).andExpect(view().name("presupuesto"));
    mvc.perform(post("/presupuesto/calcular").session(sesion).param("codigoSeguimiento", ""))
      .andExpect(status().isOk()).andExpect(view().name("presupuesto"));
    mvc.perform(post("/presupuesto/enviar").session(sesion).param("codigoSeguimiento", ""))
      .andExpect(status().isBadRequest());
    verify(servicioPresupuesto, never()).enviarPresupuesto(any(), any(), any());
  }

  @Test
  public void clienteNoPuedeOperarEnElTaller() throws Exception {
    MockHttpSession sesion = sesion("CLIENTE", 2L, "cliente@test.com");
    for (String ruta : new String[] { "/recepcion", "/tecnico/nueva-orden", "/admin/panel",
      "/ordenes/editar?idOrden=1", "/presupuesto?codigo=123456", "/cierre-orden/1" }) {
      mvc.perform(get(ruta).session(sesion)).andExpect(status().isForbidden());
    }
    mvc.perform(post("/ordenes/actualizar").session(sesion)
      .param("idOrdenReparacion", "1").param("estado", "ENTREGADO"))
      .andExpect(status().isForbidden());
    mvc.perform(post("/presupuesto/enviar").session(sesion)
      .param("codigoSeguimiento", "123456")).andExpect(status().isForbidden());
    mvc.perform(post("/cerrar-orden").session(sesion).param("idOrden", "1"))
      .andExpect(status().isForbidden());
    verifyNoInteractions(servicioCierre, servicioOrden, servicioPresupuesto);
  }

  @Test
  public void tecnicoNoPuedeModificarOrdenAjenaNiEnviarSuPresupuesto() throws Exception {
    prepararOrden();
    MockHttpSession sesion = sesion("TECNICO", 3L, "otro@test.com");
    mvc.perform(get("/ordenes/editar").session(sesion).param("idOrden", "1"))
      .andExpect(status().isForbidden());
    mvc.perform(post("/ordenes/actualizar").session(sesion)
      .param("idOrdenReparacion", "1").param("estado", "ENTREGADO"))
      .andExpect(status().isForbidden());
    mvc.perform(post("/presupuesto/enviar").session(sesion)
      .param("codigoSeguimiento", "123456")).andExpect(status().isForbidden());
    mvc.perform(post("/cerrar-orden").session(sesion).param("idOrden", "1"))
      .andExpect(status().isForbidden());
    verify(servicioOrden, never()).actualizarEstadoYNotaTecnica(any(), any(), any());
    verifyNoInteractions(servicioPresupuesto, servicioCierre);
  }

  @Test
  public void tecnicoAsignadoYAdminPuedenEditarLaOrden() throws Exception {
    prepararOrden();
    for (MockHttpSession sesion : new MockHttpSession[] {
      sesion("TECNICO", 2L, "tecnico@test.com"), sesion("ADMIN", 9L, "admin@test.com")
    }) {
      mvc.perform(get("/ordenes/editar").session(sesion).param("idOrden", "1"))
        .andExpect(status().isOk()).andExpect(view().name("editar-orden"));
      mvc.perform(post("/ordenes/actualizar").session(sesion)
        .param("idOrdenReparacion", "1").param("estado", "EN_DIAGNOSTICO"))
        .andExpect(status().is3xxRedirection());
      mvc.perform(post("/presupuesto/enviar").session(sesion)
        .param("codigoSeguimiento", "123456"))
        .andExpect(status().isOk()).andExpect(view().name("presupuesto-enviado"));
    }
    verify(servicioPresupuesto, times(2)).enviarPresupuesto(eq(123456), any(), any());
  }

  @Test
  public void comprobanteSoloEsVisibleParaClientePropietarioTecnicoAsignadoOAdmin() throws Exception {
    prepararOrden();
    for (MockHttpSession sesion : new MockHttpSession[] {
      sesion("CLIENTE", 4L, "cliente@test.com"), sesion("TECNICO", 2L, "tecnico@test.com"),
      sesion("ADMIN", 9L, "admin@test.com")
    }) {
      mvc.perform(get("/comprobante/1").session(sesion)).andExpect(status().isOk());
    }
    mvc.perform(get("/comprobante/1").session(sesion("CLIENTE", 5L, "ajeno@test.com")))
      .andExpect(status().isForbidden());
    mvc.perform(get("/comprobante/1").session(sesion("TECNICO", 3L, "otro@test.com")))
      .andExpect(status().isForbidden());
    verify(servicioCierre, times(3)).obtenerOrdenParaComprobante(1L);
  }

  @Test
  public void identificadorInvalidoOInexistenteNoEjecutaLaOperacion() throws Exception {
    MockHttpSession sesion = sesion("TECNICO", 2L, "tecnico@test.com");
    mvc.perform(post("/cerrar-orden").session(sesion).param("idOrden", "abc"))
      .andExpect(status().isBadRequest());
    mvc.perform(post("/cerrar-orden").session(sesion).param("idOrden", "99"))
      .andExpect(status().isNotFound());
    verifyNoInteractions(servicioCierre);
  }

  private void prepararOrden() throws Exception {
    OrdenReparacion orden = new OrdenReparacion();
    Usuario tecnico = new Usuario();
    tecnico.setId(2L);
    orden.setTecnicoAsignado(tecnico);
    orden.setIdOrdenReparacion(1L);
    orden.setEmailCliente("cliente@test.com");
    orden.setMontoTotal(22000.0);
    when(servicioOrden.buscarPorId(1L)).thenReturn(orden);
    when(servicioOrden.consultarEstado(123456)).thenReturn(orden);
    when(servicioCierre.obtenerOrdenParaComprobante(1L)).thenReturn(orden);
  }

  @Test
  public void transicionRechazadaMuestraErrorYNoRedirigeComoSiFueraExitosa() throws Exception {
    prepararOrden();
    doThrow(new IllegalStateException("El cambio de estado solicitado no está permitido."))
      .when(servicioOrden).actualizarEstadoYNotaTecnica(1L, com.tallerwebi.dominio.EstadoOrden.ENTREGADO, null);
    mvc.perform(post("/ordenes/actualizar").session(sesion("TECNICO", 2L, "tecnico@test.com"))
      .param("idOrdenReparacion", "1").param("estado", "ENTREGADO"))
      .andExpect(status().isConflict()).andExpect(view().name("editar-orden"))
      .andExpect(model().attributeExists("error", "orden", "ordenAActualizar", "estados"));
  }

  @Test
  public void detallePresupuestoExigeSesionYPropietarioOAsignacion() throws Exception {
    prepararOrden();
    mvc.perform(get("/presupuesto/detalle").param("codigo", "123456"))
      .andExpect(redirectedUrl("/login"));
    mvc.perform(get("/presupuesto/detalle").param("codigo", "123456")
      .session(sesion("CLIENTE", 4L, "cliente@test.com")))
      .andExpect(status().isOk()).andExpect(view().name("detalle-presupuesto"))
      .andExpect(model().attributeExists("itemsPresupuesto", "orden", "total"));
    mvc.perform(get("/presupuesto/detalle").param("codigo", "123456")
      .session(sesion("CLIENTE", 5L, "ajeno@test.com"))).andExpect(status().isForbidden());
  }

  private MockHttpSession sesion(String rol, Long id, String email) {
    Usuario usuario = new Usuario();
    usuario.setRol(rol);
    usuario.setId(id);
    usuario.setEmail(email);
    MockHttpSession sesion = new MockHttpSession();
    sesion.setAttribute("USUARIO", usuario);
    return sesion;
  }

  private static class RegistroInterceptores extends InterceptorRegistry {

    HandlerInterceptor[] obtener() {
      return getInterceptors().toArray(new HandlerInterceptor[0]);
    }
  }
}
