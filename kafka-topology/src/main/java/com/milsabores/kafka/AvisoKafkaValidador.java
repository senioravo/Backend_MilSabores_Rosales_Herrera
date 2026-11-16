package com.milsabores.kafka;

/**
 * Revisa el aviso antes de que notificación o auditoría reaccionen.
 * Un aviso vacío o incompleto no se reintenta: el listener lo rechaza
 * y el grupo sigue con el siguiente.
 */
public final class AvisoKafkaValidador {

    private AvisoKafkaValidador() {
    }

    public static void validarStock(StockDescontado aviso) {
        if (aviso == null) {
            throw new IllegalArgumentException("El aviso de stock llegó vacío");
        }
        if (aviso.ventaId() == null) {
            throw new IllegalArgumentException("El aviso de stock necesita el id de la venta");
        }
        if (aviso.items() == null || aviso.items().isEmpty()) {
            throw new IllegalArgumentException("El aviso de stock necesita al menos un producto");
        }
        for (StockDescontado.Item item : aviso.items()) {
            if (item == null || item.productoCode() == null || item.productoCode().isBlank() || item.cantidad() < 1) {
                throw new IllegalArgumentException("El aviso de stock trae un producto no válido");
            }
        }
    }

    public static void validarVenta(VentaRegistrada aviso) {
        if (aviso == null) {
            throw new IllegalArgumentException("El aviso de la venta llegó vacío");
        }
        if (aviso.ventaId() == null || aviso.usuarioId() == null) {
            throw new IllegalArgumentException("El aviso de la venta necesita la venta y el cliente");
        }
        if (aviso.estado() == null || aviso.estado().isBlank()) {
            throw new IllegalArgumentException("El aviso de la venta necesita el estado");
        }
        if (aviso.total() == null || aviso.total() < 0) {
            throw new IllegalArgumentException("El aviso de la venta necesita un total");
        }
        if (aviso.fechaCreacion() == null) {
            throw new IllegalArgumentException("El aviso de la venta necesita la fecha");
        }
        if (aviso.items() == null || aviso.items().isEmpty()) {
            throw new IllegalArgumentException("El aviso de la venta necesita al menos un producto");
        }
        for (VentaRegistrada.Item item : aviso.items()) {
            if (item == null
                    || item.productoCode() == null || item.productoCode().isBlank()
                    || item.productoNombre() == null || item.productoNombre().isBlank()
                    || item.cantidad() < 1) {
                throw new IllegalArgumentException("El aviso de la venta trae un producto no válido");
            }
        }
    }
}
