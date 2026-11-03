package com.milsabores.producto.messaging;

import com.milsabores.rabbit.RabbitTopologyNames;
import com.milsabores.rabbit.StockDescontadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicar(StockDescontadoEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitTopologyNames.TOPIC_EXCHANGE,
                RabbitTopologyNames.STOCK_EVENT_ROUTING_KEY,
                event);
        log.info("Evento publicado en {} con clave {} para venta {}",
                RabbitTopologyNames.TOPIC_EXCHANGE,
                RabbitTopologyNames.STOCK_EVENT_ROUTING_KEY,
                event.ventaId());
    }
}
