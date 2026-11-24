package com.milsabores.ventas.auditoria;

import com.milsabores.kafka.KafkaTopologyNames;
import com.milsabores.kafka.StockDescontado;
import com.milsabores.kafka.VentaRegistrada;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.annotation.KafkaListener;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditoriaKafkaListenerTest {

    @Mock
    private AuditoriaKafkaService auditoriaKafkaService;

    @Test
    void elGrupoAuditoriaLeeLosDosTopics() {
        KafkaListener anotacion = AuditoriaKafkaListener.class.getAnnotation(KafkaListener.class);

        assertEquals("${milsabores.kafka.group.auditoria}", anotacion.groupId());
        assertArrayEquals(
                new String[] {KafkaTopologyNames.STOCK_TOPIC, KafkaTopologyNames.VENTAS_TOPIC},
                anotacion.topics());
    }

    @Test
    void unAvisoDeStockPasaAlServicioDeAuditoria() {
        AuditoriaKafkaListener listener = new AuditoriaKafkaListener(auditoriaKafkaService);
        StockDescontado aviso = new StockDescontado(5L, List.of(new StockDescontado.Item("TC001", 1)));
        ConsumerRecord<String, StockDescontado> registro = new ConsumerRecord<>(
                KafkaTopologyNames.STOCK_TOPIC, 2, 7L, "5", aviso);

        listener.recibirStock(aviso, registro);

        verify(auditoriaKafkaService).registrarStockDescontado(
                aviso, KafkaTopologyNames.STOCK_TOPIC, 2, 7L);
    }

    @Test
    void unAvisoDeVentaPasaAlServicioDeAuditoria() {
        AuditoriaKafkaListener listener = new AuditoriaKafkaListener(auditoriaKafkaService);
        VentaRegistrada aviso = new VentaRegistrada(
                5L,
                9L,
                "Ana",
                "cliente@ejemplo.cl",
                "COMPLETADA",
                1000,
                LocalDateTime.of(2026, 11, 15, 12, 0),
                List.of(new VentaRegistrada.Item("TC001", "Torta", 1)));
        ConsumerRecord<String, VentaRegistrada> registro = new ConsumerRecord<>(
                KafkaTopologyNames.VENTAS_TOPIC, 1, 3L, "5", aviso);

        listener.recibirVenta(aviso, registro);

        verify(auditoriaKafkaService).registrarVenta(
                aviso, KafkaTopologyNames.VENTAS_TOPIC, 1, 3L);
    }

    @Test
    void unAvisoVacioNoLlegaAlServicio() {
        AuditoriaKafkaListener listener = new AuditoriaKafkaListener(auditoriaKafkaService);
        ConsumerRecord<String, VentaRegistrada> registro = new ConsumerRecord<>(
                KafkaTopologyNames.VENTAS_TOPIC, 1, 3L, "5", null);

        assertThrows(IllegalArgumentException.class, () -> listener.recibirVenta(null, registro));

        verifyNoInteractions(auditoriaKafkaService);
    }

    @Test
    void unAvisoDeOtroTipoNoLlegaAlServicio() {
        AuditoriaKafkaListener listener = new AuditoriaKafkaListener(auditoriaKafkaService);
        ConsumerRecord<String, String> registro = new ConsumerRecord<>(
                KafkaTopologyNames.STOCK_TOPIC, 2, 1L, "5", "texto");

        assertThrows(IllegalArgumentException.class, () -> listener.recibirDesconocido("texto", registro));

        verifyNoInteractions(auditoriaKafkaService);
    }
}
