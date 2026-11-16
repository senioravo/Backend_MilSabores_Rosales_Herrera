package com.milsabores.notificacion;

import com.milsabores.kafka.StockDescontado;
import com.milsabores.kafka.VentaRegistrada;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Reacción del grupo notificacion.
 * No se conecta a Kafka: el listener le entrega el aviso ya leído.
 */
@Slf4j
@Service
public class NotificacionKafkaService {

    public void avisarStockDescontado(StockDescontado aviso) {
        String productos = aviso.items().stream()
                .map(item -> item.productoCode() + " x" + item.cantidad())
                .collect(Collectors.joining(", "));
        log.info("Notificación: avisar que la venta {} ya tiene stock descontado ({})",
                aviso.ventaId(), productos);
    }

    public void avisarVentaRegistrada(VentaRegistrada aviso) {
        log.info("Notificación: la venta {} de {} quedó en estado {} por {}",
                aviso.ventaId(), aviso.usuarioEmail(), aviso.estado(), aviso.total());
    }
}
