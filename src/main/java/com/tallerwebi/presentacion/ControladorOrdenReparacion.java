package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.EstadoOrden;
import com.tallerwebi.dominio.OrdenReparacion;
import com.tallerwebi.dominio.ServicioOrdenReparacion;
import com.tallerwebi.dominio.excepcion.DatosIncompletosException;
import com.tallerwebi.dominio.excepcion.NoHayTecnicosDisponibles;
import com.tallerwebi.dominio.excepcion.OrdenNoEncontrado;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private static final String VISTA_RECEPCION = "recepcion";
  private final ServicioOrdenReparacion servicioOrdenReparacion;

  @Autowired
  public ControladorOrdenReparacion(ServicioOrdenReparacion servicioOrdenReparacion) {
    this.servicioOrdenReparacion = servicioOrdenReparacion;
  }

  @GetMapping("/registrar-orden")
  public ModelAndView irAFormularioRegistrarOrdenReparacion() {
    Map<String, Object> modelo = new HashMap<>();
    modelo.put(DATOS_ORDEN, new DatosOrden());
    return new ModelAndView("registro-orden-reparacion", modelo);
  }

  @PostMapping("/registrar-orden")
  public ModelAndView registrarOrdenReparacion(@ModelAttribute(DATOS_ORDEN) DatosOrden datosOrden) {
    Map<String, Object> modelo = new HashMap<>();
    try {
      OrdenReparacion orden = crearOrden(datosOrden);
      OrdenReparacion nuevaOrdenReparacion = this.servicioOrdenReparacion.registrarOrden(orden);

      modelo.put("orden", nuevaOrdenReparacion);
      modelo.put("codigoSeguimiento", nuevaOrdenReparacion.getCodigoSeguimiento());

      return new ModelAndView("confirmacion-nueva-orden-reparacion", modelo);
    } catch (DatosIncompletosException e) {
      modelo.put(
        ERROR_KEY,
        "No se puede registrar una orden con datos incompletos. Por favor, complete todos los campos."
      );
      modelo.put(DATOS_ORDEN, datosOrden);
      return new ModelAndView("registro-orden-reparacion", modelo);
    }
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
    return orden;
  }

  @RequestMapping(path = "/ordenes")
  public ModelAndView irALaListaDeOrdenes() {
    Map<String, Object> modelo = new ModelMap();
    List<OrdenReparacion> ordenes = servicioOrdenReparacion.listarTodas();
    modelo.put(ORDENES, ordenes);
    return new ModelAndView("lista-ordenes", modelo);
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

  @GetMapping("/consulta-estado")
  public ModelAndView buscarEstadoPorGet(
    @RequestParam(name = "codigoSeguimiento", required = false) Integer codigoSeguimiento
  ) {
    DatosOrden datos = new DatosOrden();
    datos.setCodigoSeguimiento(codigoSeguimiento);
    return buscarEstado(datos);
  }

  @GetMapping("/recepcion-home")
  public ModelAndView mostrarRecepcionHome() {
    Map<String, Object> model = new HashMap<>();

    model.put("datosOrden", new DatosOrden());

    return new ModelAndView("recepcion-home", model);
  }

  @PostMapping("/recepcion-home")
  public ModelAndView registrarOrdenDesdeHome(@ModelAttribute(DATOS_ORDEN) DatosOrden datosOrden) {
    Map<String, Object> model = new HashMap<>();

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
}
