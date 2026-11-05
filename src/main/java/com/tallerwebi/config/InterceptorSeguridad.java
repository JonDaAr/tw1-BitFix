package com.tallerwebi.config;

import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class InterceptorSeguridad implements HandlerInterceptor {

  @Override
  public boolean preHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler
  ) throws Exception {
    // Obtenemos el usuario de la sesión actual
    Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

    // Si no hay sesión, lo pateamos al login y cortamos la petición (return false)
    if (usuario == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return false;
    }

    // Si tiene sesión, lo dejamos pasar al controlador que pidió (return true)
    return true;
  }
}
