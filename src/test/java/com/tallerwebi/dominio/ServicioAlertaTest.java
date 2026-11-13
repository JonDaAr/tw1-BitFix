package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.ItemPresupuestoForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class ServicioAlertaTest {

    private RepositorioAlerta repositorioAlertaMock;
    private RepositorioRepuesto repositorioRepuestoMock;
    private ServicioAlerta servicioAlerta;

    @BeforeEach
    public void init() {
        repositorioAlertaMock = mock(RepositorioAlerta.class);
        repositorioRepuestoMock = mock(RepositorioRepuesto.class);
        servicioAlerta = new ServicioAlertaImpl(repositorioAlertaMock, repositorioRepuestoMock);
    }

    @Test
    public void queSePuedaCrearUnaAlertaConAccion() {
        String mensaje = "Stock bajo";
        String url = "/inventario/editar/1";

        servicioAlerta.crearAlertaConAccion(mensaje, url);

        verify(repositorioAlertaMock, times(1)).guardar(any(Alerta.class));
    }

    @Test
    public void queMarcarComoLeidaBorreLaAlertaSiExiste() {
        Long idAlerta = 1L;
        Alerta alerta = new Alerta("Alerta de prueba");

        when(repositorioAlertaMock.buscarPorId(idAlerta)).thenReturn(alerta);

        servicioAlerta.marcarComoLeida(idAlerta);

        verify(repositorioAlertaMock, times(1)).borrar(alerta);
    }

    @Test
    public void queCreeAlertaPorFaltaDeStockSiElStockEsCeroYNoExistePrevia() {
        ItemPresupuestoForm item = new ItemPresupuestoForm();
        item.setRepuestoId(10L);
        List<ItemPresupuestoForm> items = new ArrayList<>();
        items.add(item);

        Repuesto repuesto = new Repuesto();
        repuesto.setId(10L);
        repuesto.setNombre("Pantalla");
        repuesto.setStock(0);

        when(repositorioRepuestoMock.buscarPorId(10L)).thenReturn(repuesto);
        when(repositorioAlertaMock.existeAlertaPendienteConMensaje(anyString())).thenReturn(false);

        servicioAlerta.crearAlertasPorFaltaDeStock(items);

        verify(repositorioAlertaMock, times(1)).guardar(any(Alerta.class));
    }

    @Test
    public void queNoCreeAlertaSiYaExisteUnaPendienteConElMismoMensaje() {
        ItemPresupuestoForm item = new ItemPresupuestoForm();
        item.setRepuestoId(10L);
        List<ItemPresupuestoForm> items = List.of(item);

        Repuesto repuesto = new Repuesto();
        repuesto.setId(10L);
        repuesto.setNombre("Pantalla");
        repuesto.setStock(0);

        when(repositorioRepuestoMock.buscarPorId(10L)).thenReturn(repuesto);
        when(repositorioAlertaMock.existeAlertaPendienteConMensaje(anyString())).thenReturn(true);

        servicioAlerta.crearAlertasPorFaltaDeStock(items);

        verify(repositorioAlertaMock, never()).guardar(any(Alerta.class));
    }

    @Test
    public void queLimpieAlertaSiElStockVuelveASerPositivo() {
        Long idRepuesto = 5L;
        Repuesto repuesto = new Repuesto();
        repuesto.setId(idRepuesto);
        repuesto.setStock(5); // Stock positivo (> 0)

        when(repositorioRepuestoMock.buscarPorId(idRepuesto)).thenReturn(repuesto);

        servicioAlerta.limpiarAlertaSiEsNecesariaPorFaltaDeStock(idRepuesto);

        verify(repositorioAlertaMock, times(1)).eliminarPorUrlAccion("/inventario/editar/5");
    }

    @Test
    public void queLimpieAlertaCuandoSeEliminaElRepuesto() {
        Long idRepuesto = 8L;

        servicioAlerta.limpiarAlertaPorRepuestoEliminado(idRepuesto);

        verify(repositorioAlertaMock, times(1)).eliminarPorUrlAccion("/inventario/editar/8");
    }
}
