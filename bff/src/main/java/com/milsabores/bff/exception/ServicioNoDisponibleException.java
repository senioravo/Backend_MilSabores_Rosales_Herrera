package com.milsabores.bff.exception;

public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String servicio, Throwable cause) {
        super("El servicio " + servicio + " no esta disponible en este momento", cause);
    }
}
