package com.milsabores.ventas.exception;

public class VentaNotFoundException extends RuntimeException {
    public VentaNotFoundException(String mensaje) {
        super(mensaje);
    }
}
