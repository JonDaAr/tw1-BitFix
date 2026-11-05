package com.tallerwebi.presentacion;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.config.SpringWebConfig;
import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioCierreOrden;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.Usuario;
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

  @BeforeEach
  public void preparar() {
    servicioCierre = mock(ServicioCierreOrden.class);
    servicioOrden = mock(ServicioOrdenReparacion.class);
    RegistroInterceptores registro = new RegistroInterceptores();
    new SpringWebConfig().addInterceptors(registro);
    mvc = MockMvcBuilders.standaloneSetup(
      new ControladorCierreOrden(servicioCierre),
      new ControladorOrdenReparacion(servicioOrden)
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

  private static class RegistroInterceptores extends InterceptorRegistry {

    HandlerInterceptor[] obtener() {
      return getInterceptors().toArray(new HandlerInterceptor[0]);
    }
  }
}
