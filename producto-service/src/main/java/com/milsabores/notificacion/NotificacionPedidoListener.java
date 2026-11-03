package com.milsabores.notificacion;

import com.milsabores.rabbit.RabbitTopologyNames;
import com.milsabores.rabbit.StockDescontadoEvent;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NotificacionPedidoListener {

    @RabbitListener(queues = RabbitTopologyNames.NOTIFICATION_QUEUE, ackMode = "MANUAL")
    public void recibir(
            StockDescontadoEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("Notificación: avisar que la venta {} ya tiene stock descontado ({})",
                    event.ventaId(), resumen(event));
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("No se pudo notificar la venta {}", event == null ? null : event.ventaId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private static String resumen(StockDescontadoEvent event) {
        return event.items().stream()
                .map(item -> item.productoCode() + " x" + item.cantidad())
                .collect(Collectors.joining(", "));
    }
}
