package com.tallerwebi.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class InterceptorSeguridadTest {

  private final InterceptorSeguridad interceptor = new InterceptorSeguridad();

  @Test
  void apiDeAlertasRequiereSesion() throws Exception {
    MockHttpServletRequest request = solicitudAlerta();
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertFalse(interceptor.preHandle(request, response, new Object()));
    assertEquals("/spring/login", response.getRedirectedUrl());
  }

  @Test
  void apiDeAlertasRechazaRolesQueNoSeanAdmin() throws Exception {
    MockHttpServletRequest request = solicitudAlerta();
    Usuario tecnico = new Usuario();
    tecnico.setRol("TECNICO");
    request.getSession().setAttribute("USUARIO", tecnico);
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertFalse(interceptor.preHandle(request, response, new Object()));
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  void apiDeAlertasPermiteElRolAdmin() throws Exception {
    MockHttpServletRequest request = solicitudAlerta();
    Usuario admin = new Usuario();
    admin.setRol("ADMIN");
    request.getSession().setAttribute("USUARIO", admin);
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertTrue(interceptor.preHandle(request, response, new Object()));
  }

  private MockHttpServletRequest solicitudAlerta() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContextPath("/spring");
    request.setRequestURI("/spring/api/alertas/pendientes");
    request.setMethod("GET");
    return request;
  }
}
