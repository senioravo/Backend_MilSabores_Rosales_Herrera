package com.milsabores.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

/**
 * Qué hace Kafka cuando un listener falla.
 * Un fallo de base se vuelve a leer tres veces, igual que el descuento de stock
 * en RabbitMQ. Cualquier otro fallo se anota y el grupo sigue: un aviso
 * mal formado no deja la partición detenida.
 */
public final class KafkaConsumerErrors {

    /** Tres lecturas en total: la primera y dos reintentos. */
    public static final long REINTENTOS = 2L;

    public static final long ESPERA_MS = 1_000L;

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerErrors.class);

    private KafkaConsumerErrors() {
    }

    public static boolean seReintenta(Throwable error) {
        Throwable actual = error;
        while (actual != null) {
            if (actual instanceof DataAccessException) {
                return true;
            }
            actual = actual.getCause();
        }
        return false;
    }

    public static DefaultErrorHandler handler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                KafkaConsumerErrors::registrarYSeguir,
                new FixedBackOff(ESPERA_MS, REINTENTOS));
        handler.setClassifications(Map.of(DataAccessException.class, Boolean.TRUE), false);
        return handler;
    }

    public static void registrarYSeguir(ConsumerRecord<?, ?> registro, Exception error) {
        log.error("El grupo deja el aviso de {} partición {} offset {} y sigue con el siguiente",
                registro.topic(), registro.partition(), registro.offset(), error);
    }
}
