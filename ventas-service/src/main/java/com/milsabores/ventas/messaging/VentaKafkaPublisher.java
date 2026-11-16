package com.milsabores.ventas.messaging;

import com.milsabores.kafka.KafkaTopologyNames;
import com.milsabores.kafka.VentaRegistrada;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Publica el aviso de una venta ya guardada en Kafka.
 * No guarda la venta y no descuenta stock.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VentaKafkaPublisher {

    private static final int ESPERA_SEGUNDOS = 5;

    private final KafkaTemplate<String, VentaRegistrada> kafkaTemplate;

    public void publicar(VentaRegistrada aviso) throws Exception {
        String clave = aviso.ventaId().toString();
        SendResult<String, VentaRegistrada> resultado = kafkaTemplate
                .send(KafkaTopologyNames.VENTAS_TOPIC, clave, aviso)
                .get(ESPERA_SEGUNDOS, TimeUnit.SECONDS);
        if (resultado == null || resultado.getRecordMetadata() == null) {
            log.info("Aviso publicado en {} con clave {} para la venta {}",
                    KafkaTopologyNames.VENTAS_TOPIC, clave, aviso.ventaId());
            return;
        }
        log.info("Aviso publicado en {} partición {} offset {} para la venta {}",
                KafkaTopologyNames.VENTAS_TOPIC,
                resultado.getRecordMetadata().partition(),
                resultado.getRecordMetadata().offset(),
                aviso.ventaId());
    }
}
