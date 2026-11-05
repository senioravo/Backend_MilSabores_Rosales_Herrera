package com.milsabores.notificacion;

import com.milsabores.messaging.MensajeNoProcesado;
import com.milsabores.producto.config.ProductoRabbitConfig;
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
public class NotificacionDlqListener {

    @RabbitListener(
            queues = RabbitTopologyNames.NOTIFICATION_DLQ,
            ackMode = "MANUAL",
            messageConverter = ProductoRabbitConfig.DLQ_MESSAGE_CONVERTER)
    public void recibir(
            Message message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.error("Mensaje no procesado en {}: {}",
                    RabbitTopologyNames.NOTIFICATION_DLQ, MensajeNoProcesado.describir(message));
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("No se pudo registrar el mensaje de {}", RabbitTopologyNames.NOTIFICATION_DLQ, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
