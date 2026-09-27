package com.tallerwebi.presentacion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PresupuestoMultipleForm implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<ItemPresupuestoForm> items = new ArrayList<>();

  public List<ItemPresupuestoForm> getItems() {
    return items;
  }

  public void setItems(List<ItemPresupuestoForm> items) {
    this.items = items;
  }
}
