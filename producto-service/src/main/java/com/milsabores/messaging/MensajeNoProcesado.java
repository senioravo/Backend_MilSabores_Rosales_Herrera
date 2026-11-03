package com.milsabores.messaging;

import org.springframework.amqp.core.Message;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Texto de un mensaje que ya cayó en una DLQ.
 * Sirve para las tres colas: el cuerpo puede ser el comando, el evento o JSON ilegible.
 */
public final class MensajeNoProcesado {

    private MensajeNoProcesado() {
    }

    public static String describir(Message message) {
        String cuerpo = new String(message.getBody(), StandardCharsets.UTF_8);
        Map<String, Object> muerte = primeraMuerte(message.getMessageProperties().getHeaders().get("x-death"));
        Object motivo = muerte.getOrDefault("reason", "desconocido");
        Object colaOrigen = muerte.getOrDefault("queue", "desconocida");
        return "motivo=" + motivo + ", colaOrigen=" + colaOrigen + ", cuerpo=" + cuerpo;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> primeraMuerte(Object header) {
        if (header instanceof List<?> muertes
                && !muertes.isEmpty()
                && muertes.get(0) instanceof Map<?, ?> muerte) {
            return (Map<String, Object>) muerte;
        }
        return Map.of();
    }
}
