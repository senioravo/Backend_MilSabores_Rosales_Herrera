package com.milsabores.ventas.messaging;

import com.milsabores.kafka.KafkaTopologyNames;
import com.milsabores.kafka.VentaRegistrada;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaKafkaPublisherTest {

    @Mock
    private KafkaTemplate<String, VentaRegistrada> kafkaTemplate;

    @Test
    void publicaEnElTopicDeVentasConLaVentaComoClave() throws Exception {
        when(kafkaTemplate.send(eq(KafkaTopologyNames.VENTAS_TOPIC), eq("5"), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
        VentaKafkaPublisher publisher = new VentaKafkaPublisher(kafkaTemplate);
        VentaRegistrada aviso = new VentaRegistrada(
                5L,
                9L,
                "Lucía",
                "cliente@ejemplo.cl",
                "PENDIENTE",
                45000,
                LocalDateTime.of(2026, 11, 14, 19, 15),
                List.of(new VentaRegistrada.Item("TC001", "Torta Cuadrada de Chocolate", 1)));

        publisher.publicar(aviso);

        verify(kafkaTemplate).send(eq(KafkaTopologyNames.VENTAS_TOPIC), eq("5"), eq(aviso));
    }
}
