package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ControladorErrores {

  @ExceptionHandler(ResponseStatusException.class)
  public ModelAndView manejarEstadoHttp(ResponseStatusException error) {
    return vista(error.getStatusCode(), error.getReason());
  }

  @ExceptionHandler(PedidoNoEncontradoException.class)
  public ModelAndView manejarOrdenInexistente(PedidoNoEncontradoException error) {
    return vista(HttpStatus.NOT_FOUND, error.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ModelAndView manejarArgumentoInvalido(IllegalArgumentException error) {
    return vista(HttpStatus.BAD_REQUEST, error.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ModelAndView manejarEstadoInvalido(IllegalStateException error) {
    return vista(HttpStatus.CONFLICT, error.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ModelAndView manejarConflictoDeDatos() {
    return vista(HttpStatus.CONFLICT,
      "Los datos cambiaron o ya existe un registro con ese identificador. Revisá la información.");
  }

  @ExceptionHandler({ BindException.class, MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class })
  public ModelAndView manejarDatosDeFormulario() {
    return vista(HttpStatus.BAD_REQUEST, "Los datos enviados no tienen el formato requerido.");
  }

  private ModelAndView vista(HttpStatusCode estado, String mensaje) {
    ModelAndView vista = new ModelAndView("error");
    vista.setStatus(estado);
    vista.addObject("error", mensaje);
    return vista;
  }
}
