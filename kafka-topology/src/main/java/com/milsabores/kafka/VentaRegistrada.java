package com.milsabores.kafka;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Aviso de que la venta quedó guardada.
 * Lo publica ventas en milsabores.ventas. Lleva quién compró, el total y los
 * productos, que es lo que notificación y auditoría necesitan. No lleva el
 * token de Transbank.
 */
public record VentaRegistrada(
        Long ventaId,
        Long usuarioId,
        String usuarioNombre,
        String usuarioEmail,
        String estado,
        Integer total,
        LocalDateTime fechaCreacion,
        List<Item> items) {

    public VentaRegistrada {
        if (ventaId == null) {
            throw new IllegalArgumentException("El aviso de la venta necesita el id");
        }
        if (usuarioId == null) {
            throw new IllegalArgumentException("El aviso de la venta necesita el id del cliente");
        }
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El aviso de la venta necesita el estado");
        }
        if (total == null || total < 0) {
            throw new IllegalArgumentException("El aviso de la venta necesita un total");
        }
        if (fechaCreacion == null) {
            throw new IllegalArgumentException("El aviso de la venta necesita la fecha");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("El aviso de la venta necesita al menos un producto");
        }
        items = List.copyOf(items);
    }

    public record Item(String productoCode, String productoNombre, int cantidad) {

        public Item {
            if (productoCode == null || productoCode.isBlank()) {
                throw new IllegalArgumentException("El producto de la venta necesita un código");
            }
            if (productoNombre == null || productoNombre.isBlank()) {
                throw new IllegalArgumentException("El producto de la venta necesita un nombre");
            }
            if (cantidad < 1) {
                throw new IllegalArgumentException("La cantidad de " + productoCode + " debe ser al menos 1");
            }
        }
    }
}
