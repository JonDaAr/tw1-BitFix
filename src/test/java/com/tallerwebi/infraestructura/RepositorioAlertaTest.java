package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Alerta;
import com.tallerwebi.dominio.RepositorioAlerta;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioAlertaTest {


    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioAlerta repositorioAlerta;

    @BeforeEach
    public void init() {
        repositorioAlerta = new RepositorioAlertaImpl(sessionFactory);
    }


    private Alerta alerta(String mensaje, String urlAccion) {
        Alerta alerta = new Alerta(mensaje);
        alerta.setUrlAccion(urlAccion);
        return alerta;
    }


    @Test
    @Transactional
    @Rollback
    public void alertaSeGuardaYBuscaPorId() {
        Alerta alerta = alerta("Stock agotado", "/inventario/editar/1");
        repositorioAlerta.guardar(alerta);

        assertThat(alerta.getId(), notNullValue());

        Alerta buscada = repositorioAlerta.buscarPorId(alerta.getId());
        assertThat(buscada, notNullValue());
        assertThat(buscada.getMensaje(), equalTo("Stock agotado"));
        assertThat(buscada.getUrlAccion(), equalTo("/inventario/editar/1"));
    }

    @Test
    @Transactional
    @Rollback
    public void alertaDetectaSiYaExistePendienteConMismoMensaje() {
        String mensaje = "¡Atención! Sin stock.";
        Alerta alerta = alerta(mensaje, "/inventario/editar/2");

        repositorioAlerta.guardar(alerta);

        boolean existe = repositorioAlerta.existeAlertaPendienteConMensaje(mensaje);

        assertTrue(existe);
    }

    @Test
    @Transactional
    @Rollback
    public void alertasSeListanPendientesYConservanOrdenDescendente() {
        Alerta alertaVieja = alerta("Alerta vieja", "/url/1");
        alertaVieja.setFechaCreacion(LocalDateTime.now().minusHours(2));

        Alerta alertaReciente = alerta("Alerta reciente", "/url/2");
        alertaReciente.setFechaCreacion(LocalDateTime.now());

        repositorioAlerta.guardar(alertaVieja);
        repositorioAlerta.guardar(alertaReciente);

        List<Alerta> pendientes = repositorioAlerta.obtenerAlertasPendientes();

        assertThat(pendientes, hasSize(2));
        assertThat(pendientes.get(0).getMensaje(), equalTo("Alerta reciente"));
        assertThat(pendientes.get(1).getMensaje(), equalTo("Alerta vieja"));
    }

    @Test
    @Transactional
    @Rollback
    public void alertaSeEliminaCorrectamente() {
        Alerta alerta = alerta("Alerta a borrar", "/url/3");
        repositorioAlerta.guardar(alerta);
        Long id = alerta.getId();

        repositorioAlerta.borrar(alerta);

        assertThat(sessionFactory.getCurrentSession().get(Alerta.class, id), nullValue());
    }

    @Test
    @Transactional
    @Rollback
    public void alertaSeEliminaPorSuUrlAccion() {
        String url = "/inventario/editar/42";
        Alerta alerta = alerta("Sin stock", url);
        repositorioAlerta.guardar(alerta);

        repositorioAlerta.eliminarPorUrlAccion(url);

        assertThat(sessionFactory.getCurrentSession().get(Alerta.class, alerta.getId()), nullValue());
    }


}
