package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import com.tallerwebi.dominio.excepcion.OrdenNoEncontrado;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import jakarta.servlet.http.HttpSession;
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

  private final ServicioOrdenReparacion servicioOrdenReparacion;

  @Autowired
  public ControladorOrdenReparacion(ServicioOrdenReparacion servicioOrdenReparacion) {
    this.servicioOrdenReparacion = servicioOrdenReparacion;
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

  @GetMapping("/recepcion")
  public ModelAndView mostrarRecepcion() {
    Map<String, Object> model = new HashMap<>();

    model.put(DATOS_ORDEN, new DatosOrden());

    List<OrdenReparacion> ordenes = servicioOrdenReparacion.listarTodas();

    model.put(ORDENES, ordenes);

    return new ModelAndView("recepcion", model);
  }

  @PostMapping("/recepcion")
  public ModelAndView registrarOrdenDesdeRecepcion(
    @ModelAttribute(DATOS_ORDEN) DatosOrden datosOrden
  ) {
    Map<String, Object> model = new HashMap<>();

    try {
      OrdenReparacion orden = crearOrden(datosOrden);

      OrdenReparacion nuevaOrdenReparacion = servicioOrdenReparacion.registrarOrden(orden);

      model.put("orden", nuevaOrdenReparacion);
      model.put("codigoSeguimiento", nuevaOrdenReparacion.getCodigoSeguimiento());
      model.put("mostrarConfirmacion", true);

      model.put("vista", "nueva-orden");

      model.put("datosOrden", new DatosOrden());
      model.put("ordenesAsignadas", servicioOrdenReparacion.obtenerOrdenesParaTecnico());

      return new ModelAndView("panel-tecnico", model);
    } catch (NoHayTecnicosDisponibles e) {
      model.put(ERROR_KEY, "No hay técnicos disponibles para asignar la orden");

      model.put(DATOS_ORDEN, datosOrden);
      model.put("vista", "nueva-orden");

      return new ModelAndView("panel-tecnico", model);
    } catch (DatosIncompletosException e) {
      model.put(ERROR_KEY, "Por favor, complete todos los campos obligatorios.");

      model.put(DATOS_ORDEN, datosOrden);
      model.put("vista", "nueva-orden");

      return new ModelAndView("panel-tecnico", model);
    }
  }

  private OrdenReparacion crearOrden(DatosOrden datosOrden) {
    OrdenReparacion orden = new OrdenReparacion();
    orden.setNombreCliente(datosOrden.getNombreCliente());
    orden.setTelefonoCliente(datosOrden.getTelefonoCliente());
    orden.setEmailCliente(datosOrden.getEmailCliente());
    orden.setModeloEquipo(datosOrden.getModeloEquipo());
    orden.setDescripcionFalla(datosOrden.getDescripcionFalla());
    orden.setAccesorios(datosOrden.getAccesorios());

    if (datosOrden.getNivelReparacion() != null) {
      orden.setNivelReparacion(datosOrden.getNivelReparacion());
    } else {
      orden.setNivelReparacion(NivelReparacion.NIVEL_3);
    }

    return orden;
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

  @GetMapping("/recepcion-home")
  public ModelAndView mostrarRecepcionHome(HttpSession session) {
    Map<String, Object> model = new HashMap<>();
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);

    DatosOrden datosOrden = new DatosOrden();
    datosOrden.setEmailCliente(usuario.getEmail());

    model.put("datosOrden", datosOrden);

    return new ModelAndView("recepcion-home", model);
  }

  @PostMapping("/recepcion-home")
  public ModelAndView registrarOrdenDesdeHome(
    @ModelAttribute(DATOS_ORDEN) DatosOrden datosOrden,
    HttpSession session
  ) {
    Map<String, Object> model = new HashMap<>();

    // 1. Sacamos el usuario real que está logueado en este momento
    Usuario usuarioLogueado = (Usuario) session.getAttribute("USUARIO");

    // 2. Le inyectamos a la fuerza su email a la orden, ignorando lo que haya mandado el navegador
    if (usuarioLogueado != null) {
      datosOrden.setEmailCliente(usuarioLogueado.getEmail());
    }

    try {
      OrdenReparacion orden = crearOrden(datosOrden);
      servicioOrdenReparacion.registrarOrden(orden);
      return new ModelAndView("redirect:/home");
    } catch (NoHayTecnicosDisponibles e) {
      model.put(ERROR_KEY, "No hay técnicos disponibles para asignar la orden.");
      model.put(DATOS_ORDEN, datosOrden);
      return new ModelAndView("recepcion-home", model);
    } catch (DatosIncompletosException e) {
      model.put(ERROR_KEY, "Por favor, complete todos los campos obligatorios.");
      model.put(DATOS_ORDEN, datosOrden);
      return new ModelAndView("recepcion-home", model);
    }
  }

  @GetMapping("/mis-facturas")
  public ModelAndView verMisFacturas(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);

    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    // 1. Buscamos todas las órdenes del cliente
    List<OrdenReparacion> todasLasOrdenes = servicioOrdenReparacion.obtenerOrdenesDelCliente(
      usuario.getEmail()
    );

    // 2. Filtramos solo las que están en estado REPARADO o ENTREGADO
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
    // Obtenemos el usuario logueado
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    try {
      // Reutilizamos el metodo de consulta para obtener la orden validada
      OrdenReparacion orden = servicioOrdenReparacion.consultarEstado(codigoSeguimiento);

      // Validación de seguridad (IDOR) - Opcional dependiendo de tu requerimiento,
      // pero recomendado para que un cliente no vea facturas de otro.
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
}
