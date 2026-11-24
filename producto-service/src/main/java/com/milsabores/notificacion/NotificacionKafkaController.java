package com.milsabores.notificacion;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lista lo que el grupo notificacion ya guardó al consumir Kafka.
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionKafkaController {

    private final NotificacionKafkaService notificacionKafkaService;

    @GetMapping
    public List<NotificacionKafkaResponse> listar(@RequestParam(required = false) Long ventaId) {
        return notificacionKafkaService.listar(ventaId);
    }
}
