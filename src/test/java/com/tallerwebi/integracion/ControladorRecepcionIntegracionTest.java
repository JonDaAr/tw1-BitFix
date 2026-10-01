package com.tallerwebi.integracion;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tallerwebi.dominio.EstadoOrden;
import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorRecepcionIntegracionTest {

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private SessionFactory sessionFactory;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  @Transactional
  public void deberiaRegistrarOrdenYAsignarTecnicoConMenorCarga() throws Exception {
    Usuario tecnicoUno = crearTecnico("tecnico1@test.com");
    Usuario tecnicoDos = crearTecnico("tecnico2@test.com");
    sessionFactory.getCurrentSession().persist(tecnicoUno);
    sessionFactory.getCurrentSession().persist(tecnicoDos);
    crearOrdenActiva(tecnicoUno);
    crearOrdenActiva(tecnicoUno);
    sessionFactory.getCurrentSession().flush();

    mockMvc
      .perform(
        post("/recepcion")
          .param("nombreCliente", "Juan Perez")
          .param("emailCliente", "juan@test.com")
          .param("modeloEquipo", "Notebook Dell")
          .param("descripcionFalla", "No enciende")
          .param("accesorios", "Cargador")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("confirmacion-nueva-orden-reparacion"))
      .andExpect(model().attributeExists("orden"));

    OrdenReparacion ultimaOrden = sessionFactory
      .getCurrentSession()
      .createQuery("from OrdenReparacion order by id desc", OrdenReparacion.class)
      .setMaxResults(1)
      .getSingleResult();

    org.junit.jupiter.api.Assertions.assertEquals(
      tecnicoDos.getId(),
      ultimaOrden.getTecnicoAsignado().getId()
    );
  }

  private Usuario crearTecnico(String email) {
    Usuario tecnico = new Usuario();
    tecnico.setEmail(email);
    tecnico.setPassword("1234");
    tecnico.setRol("TECNICO");
    tecnico.setActivo(true);
    return tecnico;
  }

  private void crearOrdenActiva(Usuario tecnico) {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setTecnicoAsignado(tecnico);
    orden.setEstado(EstadoOrden.EN_DIAGNOSTICO);
    orden.setFechaAsignacion(java.time.LocalDateTime.now());
    sessionFactory.getCurrentSession().persist(orden);
  }
}
