package com.milsabores.notificacion;

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
class NotificacionKafkaListenerTest {

    @Mock
    private NotificacionKafkaService notificacionKafkaService;

    @Test
    void elGrupoNotificacionLeeLosDosTopics() {
        KafkaListener anotacion = NotificacionKafkaListener.class.getAnnotation(KafkaListener.class);

        assertEquals("${milsabores.kafka.group.notificacion}", anotacion.groupId());
        assertArrayEquals(
                new String[] {KafkaTopologyNames.STOCK_TOPIC, KafkaTopologyNames.VENTAS_TOPIC},
                anotacion.topics());
    }

    @Test
    void unAvisoDeStockPasaAlServicioDeNotificacion() {
        NotificacionKafkaListener listener = new NotificacionKafkaListener(notificacionKafkaService);
        StockDescontado aviso = new StockDescontado(5L, List.of(new StockDescontado.Item("TC001", 1)));
        ConsumerRecord<String, StockDescontado> registro = new ConsumerRecord<>(
                KafkaTopologyNames.STOCK_TOPIC, 1, 4L, "5", aviso);

        listener.recibirStock(aviso, registro);

        verify(notificacionKafkaService).avisarStockDescontado(aviso);
    }

    @Test
    void unAvisoDeVentaPasaAlServicioDeNotificacion() {
        NotificacionKafkaListener listener = new NotificacionKafkaListener(notificacionKafkaService);
        VentaRegistrada aviso = venta();
        ConsumerRecord<String, VentaRegistrada> registro = new ConsumerRecord<>(
                KafkaTopologyNames.VENTAS_TOPIC, 0, 2L, "5", aviso);

        listener.recibirVenta(aviso, registro);

        verify(notificacionKafkaService).avisarVentaRegistrada(aviso);
    }

    @Test
    void unAvisoVacioNoLlegaAlServicio() {
        NotificacionKafkaListener listener = new NotificacionKafkaListener(notificacionKafkaService);
        ConsumerRecord<String, StockDescontado> registro = new ConsumerRecord<>(
                KafkaTopologyNames.STOCK_TOPIC, 1, 4L, "5", null);

        assertThrows(IllegalArgumentException.class, () -> listener.recibirStock(null, registro));

        verifyNoInteractions(notificacionKafkaService);
    }

    @Test
    void unAvisoDeOtroTipoNoLlegaAlServicio() {
        NotificacionKafkaListener listener = new NotificacionKafkaListener(notificacionKafkaService);
        ConsumerRecord<String, String> registro = new ConsumerRecord<>(
                KafkaTopologyNames.VENTAS_TOPIC, 0, 9L, "5", "texto");

        assertThrows(IllegalArgumentException.class, () -> listener.recibirDesconocido("texto", registro));

        verifyNoInteractions(notificacionKafkaService);
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
