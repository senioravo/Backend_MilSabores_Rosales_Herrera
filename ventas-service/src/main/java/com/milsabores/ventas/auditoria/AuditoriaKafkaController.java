package com.milsabores.ventas.auditoria;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lista lo que el grupo auditoria ya guardó al consumir Kafka.
 * La ruta literal queda aparte de /api/ventas/{id}.
 */
@RestController
@RequestMapping("/api/ventas/auditoria")
@RequiredArgsConstructor
public class AuditoriaKafkaController {

    private final AuditoriaKafkaService auditoriaKafkaService;

    @GetMapping
    public List<AuditoriaKafkaResponse> listar(@RequestParam(required = false) Long ventaId) {
        return auditoriaKafkaService.listar(ventaId);
    }
}
