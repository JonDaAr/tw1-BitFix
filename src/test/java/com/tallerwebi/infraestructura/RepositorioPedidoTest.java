package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

import com.tallerwebi.dominio.Pedido;
import com.tallerwebi.dominio.RepositorioPedido;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioPedidoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioPedido repositorioPedido;

  @BeforeEach
  public void init() {
    repositorioPedido = new RepositorioPedidoImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void queSePuedaBuscarUnPedidoPorCodigoSeguimiento() {
    // preparacion
    Pedido pedido = new Pedido("REC-100", "EN_DIAGNOSTICO");
    sessionFactory.getCurrentSession().save(pedido);

    // ejecucion
    Pedido buscado = repositorioPedido.buscarPorCodigo("REC-100");

    // validacion
    assertThat(buscado, is(notNullValue()));
    assertThat(buscado.getCodigoSeguimiento(), is(equalTo("REC-100")));
  }
}
