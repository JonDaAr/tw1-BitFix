package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorOrdenReparacion {

  private static final String DATOS_ORDEN = "datosOrden";
  private static final String ERROR_KEY = "error";
  private static final String DATOS_CONSULTA = "datosConsulta";
  private static final String VISTA_CONSULTAR_ESTADO = "consultar-estado";
  private static final String ORDENES = "ordenes";
  private static final String ATRIBUTO_USUARIO = "USUARIO";

  // Constantes
  private static final String REDIRECT_HOME = "redirect:/home";
  private static final String REDIRECT_HOME_RECEPCION = "redirect:/home?recepcion=creada";
  private static final String REDIRECT_LOGIN = "redirect:/login";
  private static final String PARAM_CODIGO = "codigo";
  private static final String VISTA_RECEPCION_HOME = "recepcion-home";

  private final ServicioOrdenReparacion servicioOrdenReparacion;
  private final ServicioHistorialOrden servicioHistorialOrden;

  @Autowired
  public ControladorOrdenReparacion(
    ServicioOrdenReparacion servicioOrdenReparacion,
    ServicioHistorialOrden servicioHistorialOrden
  ) {
    this.servicioOrdenReparacion = servicioOrdenReparacion;
    this.servicioHistorialOrden = servicioHistorialOrden;
  }

  @GetMapping("/consultar-estado")
  public ModelAndView irAConsulta() {
    Map<String, Object> model = new HashMap<>();
    model.put(DATOS_CONSULTA, new DatosOrden());
    return new ModelAndView(VISTA_CONSULTAR_ESTADO, model);
  }

  @PostMapping("/consulta-estado")
  public ModelAndView buscarEstado(@ModelAttribute(DATOS_CONSULTA) DatosOrden datos) {
    Map<String, Object> model = new HashMap<>();

    if (datos == null || datos.getCodigoSeguimiento() == null) {
      model.put(ERROR_KEY, "Por favor, ingrese un código de seguimiento.");
      model.put(DATOS_CONSULTA, datos != null ? datos : new DatosOrden());
      return new ModelAndView(VISTA_CONSULTAR_ESTADO, model);
    }

    try {
      Integer codigoInt = datos.getCodigoSeguimiento();
      OrdenReparacion pedido = servicioOrdenReparacion.consultarEstado(codigoInt);
      model.put("pedido", pedido);
      model.put(
        "historial",
        servicioHistorialOrden.obtenerHistorial(pedido.getIdOrdenReparacion())
      );
      return new ModelAndView("resultado-consulta", model);
    } catch (NumberFormatException e) {
      model.put(ERROR_KEY, "El código de seguimiento debe ser un número válido.");
      model.put(DATOS_CONSULTA, datos);
      return new ModelAndView(VISTA_CONSULTAR_ESTADO, model);
    } catch (PedidoNoEncontradoException e) {
      model.put(ERROR_KEY, e.getMessage());
      model.put(DATOS_CONSULTA, datos);
      return new ModelAndView(VISTA_CONSULTAR_ESTADO, model);
    }
  }

  @GetMapping("/consulta-estado")
  public ModelAndView buscarEstadoPorGet(
    @RequestParam(name = "codigoSeguimiento", required = false) Integer codigoSeguimiento
  ) {
    DatosOrden datos = new DatosOrden();
    datos.setCodigoSeguimiento(codigoSeguimiento);
    return buscarEstado(datos);
  }

  // ==============================================================================
  // SE ELIMINARON LOS MÉTODOS @GetMapping("/recepcion") y @PostMapping("/recepcion")
  // ==============================================================================

  @GetMapping("/recepcion-home")
  public ModelAndView mostrarRecepcionHome(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (!esCliente(usuario)) {
      return new ModelAndView(REDIRECT_HOME);
    }
    DatosOrden datosOrden = new DatosOrden();
    datosOrden.setEmailCliente(usuario.getEmail());
    datosOrden.setNombreCliente(nombreCompleto(usuario));
    ModelAndView vista = new ModelAndView(VISTA_RECEPCION_HOME);
    vista.addObject(DATOS_ORDEN, datosOrden);
    return vista;
  }

  @PostMapping("/recepcion-home")
  public ModelAndView registrarOrdenDesdeHome(
    @ModelAttribute(DATOS_ORDEN) DatosOrden datosOrden,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (!esCliente(usuario)) {
      return new ModelAndView(REDIRECT_HOME);
    }
    datosOrden.setEmailCliente(usuario.getEmail());
    try {
      servicioOrdenReparacion.registrarOrden(crearOrden(datosOrden));
      return new ModelAndView(REDIRECT_HOME_RECEPCION);
    } catch (NoHayTecnicosDisponibles e) {
      return vistaRecepcionConError(datosOrden, "No hay técnicos disponibles para asignar la orden.");
    } catch (DatosIncompletosException e) {
      return vistaRecepcionConError(datosOrden, "Completá los campos obligatorios para continuar.");
    }
  }

  private ModelAndView vistaRecepcionConError(DatosOrden datosOrden, String mensaje) {
    ModelAndView vista = new ModelAndView(VISTA_RECEPCION_HOME);
    vista.addObject(DATOS_ORDEN, datosOrden);
    vista.addObject(ERROR_KEY, mensaje);
    return vista;
  }

  private OrdenReparacion crearOrden(DatosOrden datosOrden) {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setNombreCliente(datosOrden.getNombreCliente());
    orden.setTelefonoCliente(datosOrden.getTelefonoCliente());
    orden.setEmailCliente(datosOrden.getEmailCliente());
    orden.setModeloEquipo(datosOrden.getModeloEquipo());
    orden.setDescripcionFalla(datosOrden.getDescripcionFalla());
    orden.setAccesorios(datosOrden.getAccesorios());
    orden.setNivelReparacion(datosOrden.getNivelReparacion());
    return orden;
  }

  private boolean esCliente(Usuario usuario) {
    return usuario != null && "CLIENTE".equalsIgnoreCase(usuario.getRol());
  }

  private String nombreCompleto(Usuario usuario) {
    String nombre = usuario.getNombre() == null ? "" : usuario.getNombre().trim();
    String apellido = usuario.getApellido() == null ? "" : usuario.getApellido().trim();
    return (nombre + " " + apellido).trim();
  }

  @RequestMapping(path = "/ordenes/editar")
  public ModelAndView irAEditarOrden(@RequestParam("idOrden") Long idOrden) {
    Map<String, Object> modelo = new ModelMap();
    OrdenReparacion ordenSeleccionada = servicioOrdenReparacion.buscarPorId(idOrden);

    ActualizacionOrden ordenAActualizar = new ActualizacionOrden(
      ordenSeleccionada.getIdOrdenReparacion(),
      ordenSeleccionada.getModeloEquipo(),
      ordenSeleccionada.getEstado(),
      ordenSeleccionada.getNotaTecnica()
    );
    modelo.put("orden", ordenSeleccionada);
    modelo.put("ordenAActualizar", ordenAActualizar);
    List<EstadoOrden> estadosPermitidos = servicioOrdenReparacion.obtenerEstadosPermitidosPara(
      ordenSeleccionada.getEstado()
    );
    modelo.put("estados", estadosPermitidos);
    return new ModelAndView("editar-orden", modelo);
  }

  @RequestMapping(path = "/ordenes/actualizar", method = RequestMethod.POST)
  public ModelAndView actualizarOrden(
    @ModelAttribute("ordenAActualizar") ActualizacionOrden actualizacionOrden
  ) {
    servicioOrdenReparacion.actualizarEstadoYNotaTecnica(
      actualizacionOrden.getIdOrdenReparacion(),
      actualizacionOrden.getEstado(),
      actualizacionOrden.getNotaTecnica()
    );
    return new ModelAndView("redirect:/tecnico/panel-tecnico");
  }

  @GetMapping("/mis-facturas")
  public ModelAndView verMisFacturas(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);

    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    List<OrdenReparacion> todasLasOrdenes = servicioOrdenReparacion.obtenerOrdenesDelCliente(
      usuario.getEmail()
    );

    List<OrdenReparacion> facturas = todasLasOrdenes
      .stream()
      .filter(orden ->
        orden.getEstado() == EstadoOrden.REPARADO || orden.getEstado() == EstadoOrden.ENTREGADO
      )
      .collect(Collectors.toList());

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("facturas", facturas);

    return new ModelAndView("facturas-cliente", modelo);
  }

  @GetMapping("/comprobante")
  public ModelAndView verComprobante(
    @RequestParam("codigoSeguimiento") Integer codigoSeguimiento,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    try {
      OrdenReparacion orden = servicioOrdenReparacion.consultarEstado(codigoSeguimiento);

      if (!orden.getEmailCliente().equals(usuario.getEmail())) {
        return new ModelAndView("redirect:/mis-facturas");
      }

      Map<String, Object> model = new HashMap<>();
      model.put("orden", orden);

      return new ModelAndView("comprobante", model);
    } catch (PedidoNoEncontradoException e) {
      return new ModelAndView("redirect:/mis-facturas");
    }
  }

  // ==============================================================================
  // NUEVA LÓGICA: COTIZACIÓN TRIANGULADA (TRIAJE)
  // ==============================================================================

  @RequestMapping(path = "/solicitar-cotizacion", method = RequestMethod.GET)
  public ModelAndView irASolicitarCotizacionOnline() {
    ModelAndView mav = new ModelAndView("solicitar-cotizacion-online");
    mav.addObject("datosCotizacion", new DatosCotizacionOnline());
    return mav;
  }

  @RequestMapping(path = "/solicitar-cotizacion", method = RequestMethod.POST)
  public ModelAndView solicitarCotizacionOnline(
    @ModelAttribute("datosCotizacion") DatosCotizacionOnline datosCotizacion
  ) {
    try {
      // Este método en el Servicio debe ser modificado para asignar
      // EstadoOrden.PENDIENTE_TRIAJE, NivelReparacion.null y Tecnico.null
      OrdenReparacion nuevaOrden = servicioOrdenReparacion.registrarCotizacionOnlineParaTriaje(
        datosCotizacion
      );

      return new ModelAndView(
        "redirect:/confirmacion-cotizacion-solicitada?codigo=" + nuevaOrden.getCodigoSeguimiento()
      );
    } catch (DatosIncompletosException e) {
      ModelAndView mav = new ModelAndView("solicitar-cotizacion-online");
      mav.addObject(ERROR_KEY, "Por favor complete todos los campos obligatorios.");
      return mav;
    }
  }

  @RequestMapping(path = "/confirmacion-cotizacion-solicitada", method = RequestMethod.GET)
  public ModelAndView mostrarConfirmacionCotizacionSolicitada(
    @RequestParam(PARAM_CODIGO) Integer codigo
  ) {
    ModelAndView mav = new ModelAndView("confirmacion-cotizacion-solicitada");
    mav.addObject(PARAM_CODIGO, codigo);
    return mav;
  }

  @RequestMapping(path = "/cotizacion-online/aceptar", method = RequestMethod.POST)
  public ModelAndView aceptarCotizacionOnline(
    @RequestParam(PARAM_CODIGO) Integer codigo,
    HttpSession session
  ) throws PedidoNoEncontradoException {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    OrdenReparacion orden = servicioOrdenReparacion.consultarEstado(codigo);

    if (!orden.getEmailCliente().equals(usuario.getEmail())) {
      return new ModelAndView(REDIRECT_HOME);
    }

    servicioOrdenReparacion.actualizarEstadoYNotaTecnica(
      orden.getIdOrdenReparacion(),
      EstadoOrden.ESPERANDO_INGRESO,
      null
    );

    return new ModelAndView(REDIRECT_HOME);
  }

  @RequestMapping(path = "/cotizacion-online/rechazar", method = RequestMethod.POST)
  public ModelAndView rechazarCotizacionOnline(
    @RequestParam(PARAM_CODIGO) Integer codigo,
    HttpSession session
  ) throws PedidoNoEncontradoException {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    OrdenReparacion orden = servicioOrdenReparacion.consultarEstado(codigo);

    if (!orden.getEmailCliente().equals(usuario.getEmail())) {
      return new ModelAndView(REDIRECT_HOME);
    }

    servicioOrdenReparacion.actualizarEstadoYNotaTecnica(
      orden.getIdOrdenReparacion(),
      EstadoOrden.COTIZACION_DESCARTADA,
      null
    );

    return new ModelAndView(REDIRECT_HOME);
  }
}
