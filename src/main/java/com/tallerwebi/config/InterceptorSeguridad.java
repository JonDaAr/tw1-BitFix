package com.tallerwebi.config;

import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class InterceptorSeguridad implements HandlerInterceptor {

  @Override
  public boolean preHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler
  ) throws Exception {
    HttpSession sesion = request.getSession(false);
    Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("USUARIO");
    if (usuario == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return false;
    }
    return true;
  }
}
