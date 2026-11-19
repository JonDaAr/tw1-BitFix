package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.ItemPresupuestoForm;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioAlerta")
@Transactional
public class ServicioAlertaImpl implements ServicioAlerta {

  private final RepositorioAlerta repositorioAlerta;
  private final RepositorioRepuesto repositorioRepuesto;

  @Autowired
  public ServicioAlertaImpl(
    RepositorioAlerta repositorioAlerta,
    RepositorioRepuesto repositorioRepuesto
  ) {
    this.repositorioAlerta = repositorioAlerta;
    this.repositorioRepuesto = repositorioRepuesto;
  }

  @Override
  public void crearAlertaConAccion(String mensaje, String urlAccion) {
    Alerta nuevaAlerta = new Alerta(mensaje);
    nuevaAlerta.setUrlAccion(urlAccion);
    repositorioAlerta.guardar(nuevaAlerta);
  }

  @Override
  public List<Alerta> obtenerAlertasPendientes() {
    return repositorioAlerta.obtenerAlertasPendientes();
  }

  @Override
  public void marcarComoLeida(Long id) {
    Alerta alerta = repositorioAlerta.buscarPorId(id);
    if (alerta != null) {
      repositorioAlerta.borrar(alerta);
    }
  }

  @Override
  public void crearAlertasPorFaltaDeStock(List<ItemPresupuestoForm> listaAValidarStock) {
    if (listaAValidarStock == null) return;

    for (ItemPresupuestoForm item : listaAValidarStock) {
      if (item.getRepuestoId() != null) {
        // Buscamos el repuesto para ver su stock real después del descuento
        Repuesto repuesto = repositorioRepuesto.buscarPorId(item.getRepuestoId());

        if (repuesto != null && repuesto.getStock() == 0) {
          String mensaje =
            "¡Atención! El repuesto '" + repuesto.getNombre() + "' se ha quedado sin stock.";

          // Validamos si ya existe una alerta pendiente con este mensaje exacto (o para este repuesto)
          boolean yaExiste = repositorioAlerta.existeAlertaPendienteConMensaje(mensaje);

          if (!yaExiste) {
            String urlAccion = "/inventario/editar/" + repuesto.getId();

            this.crearAlertaConAccion(mensaje, urlAccion);
          }
        }
      }
    }
  }

  @Override
  public void limpiarAlertaSiEsNecesariaPorFaltaDeStock(Long idRepuesto) {
    Repuesto repuesto = repositorioRepuesto.buscarPorId(idRepuesto);

    if (repuesto != null && repuesto.getStock() > 0) {
      // El servicio se encarga internamente de saber cómo se forma la URL de la alerta
      String urlAccion = "/inventario/editar/" + repuesto.getId();

      // Mandamos a borrar la alerta por esa URL
      repositorioAlerta.eliminarPorUrlAccion(urlAccion);
    }
  }

  @Override
  public void limpiarAlertaPorRepuestoEliminado(Long idRepuesto) {
    if (idRepuesto != null) {
      String urlAccion = "/inventario/editar/" + idRepuesto;
      repositorioAlerta.eliminarPorUrlAccion(urlAccion);
    }
  }
}
