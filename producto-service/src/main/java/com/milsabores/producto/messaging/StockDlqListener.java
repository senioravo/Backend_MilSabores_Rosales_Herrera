package com.milsabores.producto.messaging;

import com.milsabores.messaging.MensajeNoProcesado;
import com.milsabores.rabbit.RabbitTopologyNames;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class StockDlqListener {

    @RabbitListener(queues = RabbitTopologyNames.STOCK_DLQ, ackMode = "MANUAL")
    public void recibir(
            Message message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.error("Mensaje no procesado en {}: {}",
                    RabbitTopologyNames.STOCK_DLQ, MensajeNoProcesado.describir(message));
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("No se pudo registrar el mensaje de {}", RabbitTopologyNames.STOCK_DLQ, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
