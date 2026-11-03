package com.milsabores.rabbit;

import java.util.List;

/**
 * Evento que producto publica cuando el descuento de stock ya quedó confirmado.
 * Notificación y auditoría lo reciben por el exchange topic.
 * No lleva precios ni datos de pago.
 */
public record StockDescontadoEvent(Long ventaId, List<Item> items) {

    public record Item(String productoCode, int cantidad) {
    }
}
