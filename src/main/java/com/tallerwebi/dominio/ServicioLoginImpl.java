package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.presentacion.DatosRegistro;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioOrdenReparacion repositorioOrdenReparacion;
  private static final String PREFIJO_HASH = "{pbkdf2}";
  private final Pbkdf2PasswordEncoder passwordEncoder =
    Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();

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
    if (email == null || password == null || password.isBlank()) {
      return null;
    }
    Usuario usuario = repositorioUsuario.buscar(email);
    if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
      return null;
    }
    if (!coincidePassword(password, usuario.getPassword())) {
      return null;
    }
    if (!usuario.getPassword().startsWith(PREFIJO_HASH)) {
      usuario.setPassword(codificarPassword(password));
      repositorioUsuario.modificar(usuario);
    }
    return usuario;
  }

  private boolean coincidePassword(String password, String almacenada) {
    if (almacenada == null) {
      return false;
    }
    if (almacenada.startsWith(PREFIJO_HASH)) {
      try {
        return passwordEncoder.matches(password, almacenada.substring(PREFIJO_HASH.length()));
      } catch (IllegalArgumentException e) {
        return false;
      }
    }
    // Compatibilidad temporal: solo se migra una clave antigua después de verificarla.
    return MessageDigest.isEqual(
      password.getBytes(StandardCharsets.UTF_8), almacenada.getBytes(StandardCharsets.UTF_8)
    );
  }

  private String codificarPassword(String password) {
    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("La contraseña no puede estar vacía.");
    }
    return PREFIJO_HASH + passwordEncoder.encode(password);
  }

  @Override
  public void registrar(Usuario usuario) throws UsuarioExistente {
    Usuario usuarioEncontrado = repositorioUsuario.buscar(usuario.getEmail());
    if (usuarioEncontrado != null) {
      throw new UsuarioExistente();
    }
    usuario.setPassword(codificarPassword(usuario.getPassword()));
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
        tecnico.setPassword(codificarPassword(datos.getPassword()));
      }

      repositorioUsuario.modificar(tecnico); // Asegurate de que este método exista en tu repo (puede llamarse actualizar o guardar)
    }
  }
}
