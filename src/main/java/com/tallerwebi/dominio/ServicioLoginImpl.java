package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.presentacion.DatosRegistro;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioOrdenReparacion repositorioOrdenReparacion;

  @Autowired
  public ServicioLoginImpl(
    RepositorioUsuario repositorioUsuario,
    RepositorioOrdenReparacion repositorioOrdenReparacion
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioOrdenReparacion = repositorioOrdenReparacion;
  }

  @Override
  public Usuario consultarUsuario(String email, String password) {
    return repositorioUsuario.buscarUsuario(email, password);
  }

  @Override
  public void registrar(Usuario usuario) throws UsuarioExistente {
    Usuario usuarioEncontrado = repositorioUsuario.buscarUsuario(
      usuario.getEmail(),
      usuario.getPassword()
    );
    if (usuarioEncontrado != null) {
      throw new UsuarioExistente();
    }
    repositorioUsuario.guardar(usuario);
  }

  @Override
  public List<Usuario> obtenerUsuariosPorRol(String rol) {
    return repositorioUsuario.buscarPorRol(rol);
  }

  @Override
  public void eliminarUsuario(Long id) throws IllegalStateException {
    // 1. Verificamos si tiene órdenes activas
    long ordenesPendientes = repositorioOrdenReparacion.contarOrdenesActivas(id);

    if (ordenesPendientes > 0) {
      throw new IllegalStateException("El técnico tiene órdenes activas.");
    }

    // 2. Si no tiene, procedemos con la baja lógica
    Usuario usuario = repositorioUsuario.buscarPorId(id);
    if (usuario != null) {
      usuario.setActivo(false);
      repositorioUsuario.modificar(usuario);
    }
  }

  @Override
  public List<Usuario> obtenerTecnicosActivos() {
    return repositorioUsuario.buscarTecnicosActivos();
  }

  @Override
  public Usuario buscarUsuarioPorId(Long id) {
    return repositorioUsuario.buscarPorId(id);
  }

  @Override
  @Transactional
  public void actualizarDatosTecnico(Long id, DatosRegistro datos) {
    Usuario tecnico = repositorioUsuario.buscarPorId(id);
    if (tecnico != null) {
      tecnico.setNombre(datos.getNombre());
      tecnico.setApellido(datos.getApellido());
      tecnico.setEmail(datos.getEmail());
      tecnico.setNivelSoporte(datos.getNivelSoporte());

      // Solo actualizamos la contraseña si el admin escribió una nueva
      if (datos.getPassword() != null && !datos.getPassword().trim().isEmpty()) {
        tecnico.setPassword(datos.getPassword());
      }

      repositorioUsuario.modificar(tecnico); // Asegurate de que este método exista en tu repo (puede llamarse actualizar o guardar)
    }
  }
}
