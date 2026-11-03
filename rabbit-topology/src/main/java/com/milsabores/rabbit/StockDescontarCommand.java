package com.milsabores.rabbit;

import java.util.List;

/**
 * Comando que ventas publica cuando la venta ya quedó guardada.
 * Producto lo usa para descontar stock. No lleva precios ni datos de pago.
 */
public record StockDescontarCommand(Long ventaId, List<Item> items) {

    public record Item(String productoCode, int cantidad) {
    }
}
