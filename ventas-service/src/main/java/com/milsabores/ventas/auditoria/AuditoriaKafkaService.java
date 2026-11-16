package com.milsabores.ventas.auditoria;

import com.milsabores.kafka.StockDescontado;
import com.milsabores.kafka.VentaRegistrada;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Reacción del grupo auditoria.
 * No se conecta a Kafka: el listener le entrega el aviso ya leído.
 */
@Slf4j
@Service
public class AuditoriaKafkaService {

    public void registrarStockDescontado(StockDescontado aviso) {
        String productos = aviso.items().stream()
                .map(item -> item.productoCode() + " x" + item.cantidad())
                .collect(Collectors.joining(", "));
        log.info("Auditoría: registrar la venta {} con stock descontado ({})",
                aviso.ventaId(), productos);
    }

    public void registrarVenta(VentaRegistrada aviso) {
        log.info("Auditoría: registrar la venta {} de {} en estado {} por {} el {}",
                aviso.ventaId(), aviso.usuarioEmail(), aviso.estado(), aviso.total(), aviso.fechaCreacion());
    }
}
