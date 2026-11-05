package com.tallerwebi.presentacion;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class ControladorConflictosTest {

  @Test
  public void envioRepetidoMuestraErrorEnElFormulario() throws Exception {
    ServicioPresupuesto servicio = mock(ServicioPresupuesto.class);
    doThrow(new IllegalStateException("El presupuesto ya fue enviado."))
      .when(servicio).enviarPresupuesto(eq(123456), any(), any());
    MockMvc mvc = presupuesto(servicio);
    mvc.perform(post("/presupuesto/enviar").param("codigoSeguimiento", "123456"))
      .andExpect(status().isOk()).andExpect(view().name("presupuesto"))
      .andExpect(model().attributeExists("error", "form", "repuestos", "codigoSeguimiento"));
  }

  @Test
  public void conflictoAlReservarResponde409() throws Exception {
    ServicioPresupuesto servicio = mock(ServicioPresupuesto.class);
    doThrow(new ObjectOptimisticLockingFailureException(OrdenReparacion.class, 1L))
      .when(servicio).enviarPresupuesto(eq(123456), any(), any());
    presupuesto(servicio).perform(post("/presupuesto/enviar").param("codigoSeguimiento", "123456"))
      .andExpect(status().isConflict());
  }

  @Test
  public void conflictoDeInventarioYRepuestoReferenciadoResponden409() throws Exception {
    ServicioInventario servicio = mock(ServicioInventario.class);
    MockMvc mvc = MockMvcBuilders.standaloneSetup(new ControladorInventario(servicio))
      .setControllerAdvice(new ControladorConflictos()).build();
    Usuario admin = new Usuario();
    admin.setRol("ADMIN");
    MockHttpSession sesion = new MockHttpSession();
    sesion.setAttribute("USUARIO", admin);
    doThrow(new ObjectOptimisticLockingFailureException(Repuesto.class, 1L))
      .when(servicio).guardarOActualizar(any());
    mvc.perform(post("/inventario/guardar").session(sesion)).andExpect(status().isConflict());
    doThrow(new org.springframework.dao.DataIntegrityViolationException("Repuesto referenciado"))
      .when(servicio).eliminar(1L);
    mvc.perform(post("/inventario/eliminar/1").session(sesion)).andExpect(status().isConflict());
  }

  private MockMvc presupuesto(ServicioPresupuesto servicio) {
    return MockMvcBuilders.standaloneSetup(
      new ControladorPresupuesto(servicio, mock(ServicioOrdenReparacion.class)))
      .setControllerAdvice(new ControladorConflictos())
      .setViewResolvers(new InternalResourceViewResolver("/WEB-INF/views/thymeleaf/", ".html"))
      .build();
  }
}
