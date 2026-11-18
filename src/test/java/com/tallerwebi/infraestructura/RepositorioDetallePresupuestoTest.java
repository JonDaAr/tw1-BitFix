package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.DetallePresupuesto;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RepositorioDetallePresupuestoTest {

    private SessionFactory sessionFactoryMock;
    private Session sessionMock;
    private RepositorioDetallePresupuestoImpl repositorio;

    @BeforeEach
    public void init() {
        sessionFactoryMock = mock(SessionFactory.class);
        sessionMock = mock(Session.class);
        when(sessionFactoryMock.getCurrentSession()).thenReturn(sessionMock);
        repositorio = new RepositorioDetallePresupuestoImpl(sessionFactoryMock);
    }

    @Test
    public void queGuardeDetallePresupuesto() {
        DetallePresupuesto detalle = new DetallePresupuesto();

        repositorio.guardar(detalle);

        verify(sessionMock).save(detalle);
    }

    @Test
    public void queModifiqueDetallePresupuesto() {
        DetallePresupuesto detalle = new DetallePresupuesto();

        repositorio.modificar(detalle);

        verify(sessionMock).merge(detalle);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void queBusqueDetallesPorIdDeOrden() {
        Long ordenId = 1L;
        Query<DetallePresupuesto> queryMock = mock(Query.class);
        List<DetallePresupuesto> esperados = List.of(new DetallePresupuesto());

        when(
                sessionMock.createQuery(
                        "from DetallePresupuesto d where d.ordenReparacion.idOrdenReparacion = :ordenId",
                        DetallePresupuesto.class
                )
        ).thenReturn(queryMock);
        when(queryMock.setParameter("ordenId", ordenId)).thenReturn(queryMock);
        when(queryMock.getResultList()).thenReturn(esperados);

        List<DetallePresupuesto> resultado = repositorio.buscarPorOrden(ordenId);

        assertThat(resultado, is(equalTo(esperados)));
    }
}