package com.milsabores.ventas.auditoria;

import java.time.LocalDateTime;

public record AuditoriaKafkaResponse(
        Long id,
        Long ventaId,
        String tipo,
        String resumen,
        String topic,
        int particion,
        long offset,
        LocalDateTime recibidoEn) {
}
