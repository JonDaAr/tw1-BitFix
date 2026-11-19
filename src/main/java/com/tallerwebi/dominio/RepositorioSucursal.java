package com.tallerwebi.dominio;

import java.util.List;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface RepositorioSucursal {
  List<Sucursal> obtenerTodas();
}
