package com.tallerwebi.dominio.excepcion;

public class OrdenNoExiste extends RuntimeException {
    public OrdenNoExiste(String message) {
        super(message);
    }
}
