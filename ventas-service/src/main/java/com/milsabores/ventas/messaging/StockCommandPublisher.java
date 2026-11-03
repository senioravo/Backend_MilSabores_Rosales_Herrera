package com.milsabores.ventas.messaging;

import com.milsabores.rabbit.RabbitTopologyNames;
import com.milsabores.rabbit.StockDescontarCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockCommandPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicar(StockDescontarCommand command) {
        rabbitTemplate.convertAndSend(
                RabbitTopologyNames.DIRECT_EXCHANGE,
                RabbitTopologyNames.STOCK_ROUTING_KEY,
                command);
        log.info("Comando publicado en {} con clave {} para venta {}",
                RabbitTopologyNames.DIRECT_EXCHANGE,
                RabbitTopologyNames.STOCK_ROUTING_KEY,
                command.ventaId());
    }
}
