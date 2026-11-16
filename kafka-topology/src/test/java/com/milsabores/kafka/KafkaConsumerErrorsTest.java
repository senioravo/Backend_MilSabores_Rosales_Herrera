package com.milsabores.kafka;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KafkaConsumerErrorsTest {

    @Test
    void unFalloDeBaseSeReintentaTresVeces() throws Exception {
        assertTrue(KafkaConsumerErrors.seReintenta(new DataAccessResourceFailureException("base caída")));
        assertTrue(KafkaConsumerErrors.seReintenta(
                new IllegalStateException(new DataAccessResourceFailureException("base caída"))));

        FixedBackOff espera = esperaDe(KafkaConsumerErrors.handler());
        assertEquals(KafkaConsumerErrors.ESPERA_MS, espera.getInterval());
        assertEquals(KafkaConsumerErrors.REINTENTOS, espera.getMaxAttempts());
    }

    private static FixedBackOff esperaDe(DefaultErrorHandler handler) throws Exception {
        Object tracker = campo(handler, "failureTracker");
        return (FixedBackOff) campo(tracker, "backOff");
    }

    private static Object campo(Object objeto, String nombre) throws Exception {
        Class<?> tipo = objeto.getClass();
        while (tipo != null) {
            try {
                Field campo = tipo.getDeclaredField(nombre);
                campo.setAccessible(true);
                return campo.get(objeto);
            } catch (NoSuchFieldException ignored) {
                tipo = tipo.getSuperclass();
            }
        }
        throw new NoSuchFieldException(nombre);
    }

    @Test
    void unAvisoInvalidoNoSeReintenta() {
        assertFalse(KafkaConsumerErrors.seReintenta(new IllegalArgumentException("aviso vacío")));
    }

    @Test
    void unAvisoDejadoAtrasQuedaEnElLog() {
        Logger logger = (Logger) LoggerFactory.getLogger(KafkaConsumerErrors.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            ConsumerRecord<String, String> registro = new ConsumerRecord<>(
                    KafkaTopologyNames.VENTAS_TOPIC, 1, 8L, "5", "{");

            KafkaConsumerErrors.registrarYSeguir(registro, new IllegalArgumentException("JSON cortado"));

            assertEquals(1, appender.list.size());
            String mensaje = appender.list.get(0).getFormattedMessage();
            assertTrue(mensaje.contains(KafkaTopologyNames.VENTAS_TOPIC));
            assertTrue(mensaje.contains("offset 8"));
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    void unAvisoVacioSeRechazaAntesDeReaccionar() {
        assertThrows(IllegalArgumentException.class, () -> AvisoKafkaValidador.validarStock(null));
        assertThrows(IllegalArgumentException.class, () -> AvisoKafkaValidador.validarVenta(null));
    }

    @Test
    void unAvisoCompletoPasaLaValidacion() {
        AvisoKafkaValidador.validarStock(new StockDescontado(5L, java.util.List.of(new StockDescontado.Item("TC001", 1))));
        AvisoKafkaValidador.validarVenta(new VentaRegistrada(
                5L,
                9L,
                "Ana",
                "cliente@ejemplo.cl",
                "PENDIENTE",
                1000,
                java.time.LocalDateTime.of(2026, 11, 15, 12, 0),
                java.util.List.of(new VentaRegistrada.Item("TC001", "Torta", 1))));
    }
}
