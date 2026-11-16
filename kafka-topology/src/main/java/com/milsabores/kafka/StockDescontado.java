package com.milsabores.kafka;

import java.util.List;

/**
 * Aviso de que el stock de una venta ya se descontó.
 * Lo publica productos en milsabores.stock. No lleva precios ni datos de pago:
 * notificación y auditoría solo necesitan saber qué productos se movieron.
 */
public record StockDescontado(Long ventaId, List<Item> items) {

    public StockDescontado {
        if (ventaId == null) {
            throw new IllegalArgumentException("El aviso de stock necesita el id de la venta");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("El aviso de stock necesita al menos un producto");
        }
        items = List.copyOf(items);
    }

    public record Item(String productoCode, int cantidad) {

        public Item {
            if (productoCode == null || productoCode.isBlank()) {
                throw new IllegalArgumentException("El producto del aviso de stock necesita un código");
            }
            if (cantidad < 1) {
                throw new IllegalArgumentException("La cantidad de " + productoCode + " debe ser al menos 1");
            }
        }
    }
}
