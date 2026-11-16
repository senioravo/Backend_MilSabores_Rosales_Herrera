package com.milsabores.kafka;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvisosKafkaTest {

    @Test
    void elAvisoDeStockGuardaLaVentaYLosProductos() {
        StockDescontado aviso = new StockDescontado(5L, List.of(new StockDescontado.Item("TC001", 1)));

        assertEquals(5L, aviso.ventaId());
        assertEquals("TC001", aviso.items().get(0).productoCode());
        assertEquals(1, aviso.items().get(0).cantidad());
    }

    @Test
    void elAvisoDeStockRechazaUnaVentaSinProductos() {
        assertThrows(IllegalArgumentException.class, () -> new StockDescontado(5L, List.of()));
    }

    @Test
    void elAvisoDeLaVentaGuardaAlClienteYElTotal() {
        VentaRegistrada aviso = new VentaRegistrada(
                5L,
                9L,
                "Lucía",
                "cliente@ejemplo.cl",
                "PENDIENTE",
                45000,
                LocalDateTime.of(2026, 11, 14, 19, 15),
                List.of(new VentaRegistrada.Item("TC001", "Torta Cuadrada de Chocolate", 1)));

        assertEquals("PENDIENTE", aviso.estado());
        assertEquals(45000, aviso.total());
        assertEquals("Torta Cuadrada de Chocolate", aviso.items().get(0).productoNombre());
    }

    @Test
    void elAvisoDeLaVentaRechazaUnTotalNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new VentaRegistrada(
                5L,
                9L,
                "Lucía",
                "cliente@ejemplo.cl",
                "PENDIENTE",
                -1,
                LocalDateTime.now(),
                List.of(new VentaRegistrada.Item("TC001", "Torta", 1))));
    }
}
