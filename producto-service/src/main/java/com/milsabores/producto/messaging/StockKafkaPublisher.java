package com.milsabores.producto.messaging;

import com.milsabores.kafka.KafkaTopologyNames;
import com.milsabores.kafka.StockDescontado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Publica el aviso de stock ya descontado en Kafka.
 * No descuenta stock y no confirma el mensaje de RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockKafkaPublisher {

    private static final int ESPERA_SEGUNDOS = 5;

    private final KafkaTemplate<String, StockDescontado> kafkaTemplate;

    public void publicar(StockDescontado aviso) throws Exception {
        String clave = aviso.ventaId().toString();
        SendResult<String, StockDescontado> resultado = kafkaTemplate
                .send(KafkaTopologyNames.STOCK_TOPIC, clave, aviso)
                .get(ESPERA_SEGUNDOS, TimeUnit.SECONDS);
        if (resultado == null || resultado.getRecordMetadata() == null) {
            log.info("Aviso publicado en {} con clave {} para la venta {}",
                    KafkaTopologyNames.STOCK_TOPIC, clave, aviso.ventaId());
            return;
        }
        log.info("Aviso publicado en {} partición {} offset {} para la venta {}",
                KafkaTopologyNames.STOCK_TOPIC,
                resultado.getRecordMetadata().partition(),
                resultado.getRecordMetadata().offset(),
                aviso.ventaId());
    }
}
