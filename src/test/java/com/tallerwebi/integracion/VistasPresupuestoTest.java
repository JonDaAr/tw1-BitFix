package com.tallerwebi.integracion;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.presentacion.ItemPresupuestoForm;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
@Transactional
@Rollback
public class VistasPresupuestoTest {

  @Autowired
  private WebApplicationContext contexto;
  @Autowired
  private SessionFactory factory;
  @Autowired
  private ServicioPresupuesto presupuesto;

  @Test
  public void detalleYComprobanteRenderizanLosPreciosHistoricos() throws Exception {
    Usuario cliente = new Usuario();
    cliente.setEmail("vista-detalle@test.com");
    cliente.setRol("CLIENTE");
    cliente.setNombre("Cliente de prueba");
    cliente.setActivo(true);
    factory.getCurrentSession().persist(cliente);
    Repuesto repuesto = new Repuesto("RAM de prueba", 1000.0, 5);
    factory.getCurrentSession().persist(repuesto);
    OrdenReparacion orden = new OrdenReparacion("Cliente", "123", "PC", "No enciende");
    orden.setCodigoSeguimiento(654321);
    orden.setEmailCliente(cliente.getEmail());
    factory.getCurrentSession().persist(orden);
    presupuesto.enviarPresupuesto(654321, List.of(new ItemPresupuestoForm(repuesto.getId(), 2)), 22000.0);
    repuesto.setNombre("Nombre posterior");
    repuesto.setPrecio(9000.0);
    MockHttpSession sesion = new MockHttpSession();
    sesion.setAttribute("USUARIO", cliente);
    var mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    mvc.perform(get("/presupuesto/detalle").param("codigo", "654321").session(sesion))
      .andExpect(status().isOk()).andExpect(content().string(containsString("RAM de prueba")));
    mvc.perform(get("/comprobante").param("codigoSeguimiento", "654321").session(sesion))
      .andExpect(status().isOk()).andExpect(content().string(containsString("RAM de prueba")));
  }

  @Test
  public void ordenInexistenteYCodigoMalFormadoRenderizanErroresControlados() throws Exception {
    var mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    mvc.perform(get("/cierre-orden/999999"))
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("No se pudo completar la operación")));
    mvc.perform(get("/consulta-estado").param("codigoSeguimiento", "no-es-numero"))
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("formato requerido")));
  }
}
