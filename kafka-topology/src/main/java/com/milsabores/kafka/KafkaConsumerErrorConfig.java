package com.milsabores.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;

/**
 * La regla de errores queda fuera de notificación y de auditoría.
 * Esos servicios solo reaccionan al aviso que ya pasó la validación.
 */
@Configuration
public class KafkaConsumerErrorConfig {

    @Bean
    public CommonErrorHandler kafkaConsumerErrorHandler() {
        return KafkaConsumerErrors.handler();
    }
}
