package com.tallerwebi.config;

import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

public class InterceptorPermisos implements HandlerInterceptor {

  private static final String RUTA_DETALLE_PRESUPUESTO = "/presupuesto/detalle";
  private static final int CANTIDAD_IDENTIFICADORES = 1;

  private static final Set<String> RUTAS_CLIENTE = Set.of(
    "/presupuesto/aceptar", "/presupuesto/rechazar"
  );
  private static final Set<String> RUTAS_ORDEN = Set.of(
    "/ordenes/editar", "/ordenes/actualizar", "/cierre-orden/{idOrden}",
    "/cerrar-orden", "/comprobante/{idOrden}"
  );
  private final ServicioOrdenReparacion servicioOrden;

  public InterceptorPermisos(ServicioOrdenReparacion servicioOrden) {
    this.servicioOrden = servicioOrden;
  }

  @Override
  public boolean preHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler
  ) throws Exception {
    HttpSession sesion = request.getSession(false);
    Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("USUARIO");
    if (usuario == null) {
      return responderError(response, HttpServletResponse.SC_FORBIDDEN);
    }
    String ruta = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    if (!tieneRolPermitido(usuario, ruta)) {
      return responderError(response, HttpServletResponse.SC_FORBIDDEN);
    }
    if (requiereOrden(ruta)) {
      return validarAccesoAOrden(request, response, usuario, ruta);
    }
    return true;
  }

  private boolean tieneRolPermitido(Usuario usuario, String ruta) {
    if (ruta == null) {
      return false;
    }
    boolean admin = esRol(usuario, "ADMIN");
    if (ruta.startsWith("/admin")) {
      return admin;
    }
    if (RUTAS_CLIENTE.contains(ruta)) {
      return esRol(usuario, "CLIENTE");
    }
    if ("/home".equals(ruta) || "/recepcion-home".equals(ruta)) {
      return esPersonal(usuario) || esRol(usuario, "CLIENTE");
    }
    if (ruta.startsWith("/comprobante") || RUTA_DETALLE_PRESUPUESTO.equals(ruta)) {
      return esPersonal(usuario) || esRol(usuario, "CLIENTE");
    }
    return esPersonal(usuario);
  }

  private boolean requiereOrden(String ruta) {
    return RUTAS_ORDEN.contains(ruta) || esPresupuesto(ruta);
  }

  private boolean esPresupuesto(String ruta) {
    return Set.of("/presupuesto", "/presupuesto/calcular", "/presupuesto/enviar", RUTA_DETALLE_PRESUPUESTO).contains(ruta);
  }

  private boolean validarAccesoAOrden(
    HttpServletRequest request, HttpServletResponse response, Usuario usuario, String ruta
  ) throws IOException {
    OrdenReparacion orden;
    try {
      String identificador = obtenerIdentificador(request, ruta);
      if (identificador == null && presupuestoSinOrdenPermitido(ruta)) {
        return true;
      }
      orden = esPresupuesto(ruta)
        ? servicioOrden.consultarEstado(Integer.valueOf(identificador))
        : servicioOrden.buscarPorId(Long.valueOf(identificador));
    } catch (NumberFormatException e) {
      return responderError(response, HttpServletResponse.SC_BAD_REQUEST);
    } catch (PedidoNoEncontradoException e) {
      return responderError(response, HttpServletResponse.SC_NOT_FOUND);
    }
    if (orden == null) {
      return responderError(response, HttpServletResponse.SC_NOT_FOUND);
    }
    if (puedeAccederAOrden(usuario, orden, ruta)) {
      return true;
    }
    return responderError(response, HttpServletResponse.SC_FORBIDDEN);
  }

  private boolean presupuestoSinOrdenPermitido(String ruta) {
    return "/presupuesto".equals(ruta) || "/presupuesto/calcular".equals(ruta);
  }

  private boolean puedeAccederAOrden(Usuario usuario, OrdenReparacion orden, String ruta) {
    if (esRol(usuario, "ADMIN")) {
      return true;
    }
    if (esRol(usuario, "TECNICO")) {
      return esTecnicoAsignado(usuario, orden);
    }
    boolean consultaCliente = "/comprobante/{idOrden}".equals(ruta) || RUTA_DETALLE_PRESUPUESTO.equals(ruta);
    return consultaCliente && esClientePropietario(usuario, orden);
  }

  private boolean esTecnicoAsignado(Usuario usuario, OrdenReparacion orden) {
    if (usuario.getId() == null || orden.getTecnicoAsignado() == null) {
      return false;
    }
    return usuario.getId().equals(orden.getTecnicoAsignado().getId());
  }

  private boolean esClientePropietario(Usuario usuario, OrdenReparacion orden) {
    return usuario.getEmail() != null && usuario.getEmail().equals(orden.getEmailCliente());
  }

  private String obtenerIdentificador(HttpServletRequest request, String ruta) {
    if (ruta.contains("{idOrden}")) {
      Object variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
      return variables instanceof Map<?, ?> mapa ? (String) mapa.get("idOrden") : null;
    }
    String parametro = parametroParaRuta(ruta);
    String[] valores = request.getParameterValues(parametro);
    if (valores == null) {
      return null;
    }
    // La autorización y el controlador deben recibir un único identificador.
    if (valores.length != CANTIDAD_IDENTIFICADORES) {
      throw new NumberFormatException("Identificador repetido");
    }
    return valores[0].isBlank() ? null : valores[0];
  }

  private String parametroParaRuta(String ruta) {
    return switch (ruta) {
      case "/presupuesto", RUTA_DETALLE_PRESUPUESTO -> "codigo";
      case "/presupuesto/calcular", "/presupuesto/enviar" -> "codigoSeguimiento";
      case "/ordenes/actualizar" -> "idOrdenReparacion";
      default -> "idOrden";
    };
  }

  private boolean esPersonal(Usuario usuario) {
    return esRol(usuario, "ADMIN") || esRol(usuario, "TECNICO");
  }

  private boolean esRol(Usuario usuario, String rol) {
    return rol.equalsIgnoreCase(usuario.getRol());
  }

  private boolean responderError(HttpServletResponse response, int estado) throws IOException {
    response.sendError(estado);
    return false;
  }
}
