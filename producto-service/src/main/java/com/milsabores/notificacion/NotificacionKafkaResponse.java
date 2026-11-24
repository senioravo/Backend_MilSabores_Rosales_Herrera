package com.milsabores.notificacion;

import java.time.LocalDateTime;

public record NotificacionKafkaResponse(
        Long id,
        Long ventaId,
        String tipo,
        String resumen,
        String topic,
        int particion,
        long offset,
        LocalDateTime recibidoEn) {
}
