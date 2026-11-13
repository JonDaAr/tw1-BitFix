package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Alerta;
import com.tallerwebi.dominio.ServicioAlerta;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

public class ControladorAlertaTest {

    private ServicioAlerta servicioAlertaMock;
    private ControladorAlerta controladorAlerta;

    @BeforeEach
    public void init() {
        servicioAlertaMock = mock(ServicioAlerta.class);
        controladorAlerta = new ControladorAlerta(servicioAlertaMock);
    }

    @Test
    public void obtenerAlertasPendientesRetornaListaDeAlertas() {
        List<Alerta> alertasMock = List.of(new Alerta("Alerta 1"), new Alerta("Alerta 2"));
        when(servicioAlertaMock.obtenerAlertasPendientes()).thenReturn(alertasMock);

        ResponseEntity<List<Alerta>> respuesta = controladorAlerta.obtenerPendientes();

        assertThat(respuesta.getStatusCode().is2xxSuccessful(), is(true));
        assertThat(respuesta.getBody(), hasSize(2));
        verify(servicioAlertaMock, times(1)).obtenerAlertasPendientes();
    }

    @Test
    public void marcarComoLeidaRetornaStatusOkYEjecutaServicio() {
        Long idAlerta = 1L;

        ResponseEntity<Void> respuesta = controladorAlerta.marcarComoLeida(idAlerta);

        assertThat(respuesta.getStatusCode().is2xxSuccessful(), is(true));
        verify(servicioAlertaMock, times(1)).marcarComoLeida(idAlerta);
    }
}