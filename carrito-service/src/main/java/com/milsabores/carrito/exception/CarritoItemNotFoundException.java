package com.milsabores.carrito.exception;

public class CarritoItemNotFoundException extends RuntimeException {
    public CarritoItemNotFoundException(String message) {
        super(message);
    }
}
