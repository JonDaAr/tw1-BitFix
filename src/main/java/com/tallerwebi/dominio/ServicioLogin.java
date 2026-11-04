package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.presentacion.DatosRegistro;
import java.util.List;

public interface ServicioLogin {
  Usuario consultarUsuario(String email, String password);
  void registrar(Usuario usuario) throws UsuarioExistente;

  List<Usuario> obtenerUsuariosPorRol(String tecnico);

  void eliminarUsuario(Long id) throws IllegalStateException;

  List<Usuario> obtenerTecnicosActivos();
  Usuario buscarUsuarioPorId(Long id);
  void actualizarDatosTecnico(Long id, DatosRegistro datos);
}
