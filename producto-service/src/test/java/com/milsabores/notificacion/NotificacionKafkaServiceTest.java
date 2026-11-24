package com.milsabores.notificacion;

import com.milsabores.kafka.VentaRegistrada;
import com.milsabores.producto.model.NotificacionKafka;
import com.milsabores.producto.repository.NotificacionKafkaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionKafkaServiceTest {

    @Mock
    private NotificacionKafkaRepository repository;

    @InjectMocks
    private NotificacionKafkaService service;

    @Test
    void guardaLaVentaLaPrimeraVez() {
        when(repository.existsByTopicAndParticionAndKafkaOffset("milsabores.ventas", 0, 2L)).thenReturn(false);

        service.avisarVentaRegistrada(venta(), "milsabores.ventas", 0, 2L);

        ArgumentCaptor<NotificacionKafka> captor = ArgumentCaptor.forClass(NotificacionKafka.class);
        verify(repository).save(captor.capture());
        assertEquals(5L, captor.getValue().getVentaId());
        assertEquals("VENTA_REGISTRADA", captor.getValue().getTipo());
        assertEquals(2L, captor.getValue().getKafkaOffset());
    }

    @Test
    void noDuplicaElMismoOffset() {
        when(repository.existsByTopicAndParticionAndKafkaOffset("milsabores.ventas", 0, 2L)).thenReturn(true);

        service.avisarVentaRegistrada(venta(), "milsabores.ventas", 0, 2L);

        verify(repository, never()).save(any());
    }

    private static VentaRegistrada venta() {
        return new VentaRegistrada(
                5L,
                9L,
                "Ana",
                "cliente@ejemplo.cl",
                "PENDIENTE",
                1000,
                LocalDateTime.of(2026, 11, 15, 12, 0),
                List.of(new VentaRegistrada.Item("TC001", "Torta", 1)));
    }
}
