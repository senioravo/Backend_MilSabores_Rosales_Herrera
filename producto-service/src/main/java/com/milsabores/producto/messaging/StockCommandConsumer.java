package com.milsabores.producto.messaging;

import com.milsabores.producto.exception.ProductoNotFoundException;
import com.milsabores.producto.service.ProductoService;
import com.milsabores.rabbit.RabbitTopologyNames;
import com.milsabores.rabbit.StockDescontadoEvent;
import com.milsabores.rabbit.StockDescontarCommand;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockCommandConsumer {

    private final ProductoService productoService;
    private final RetryTemplate stockRetryTemplate;
    private final StockEventPublisher stockEventPublisher;

    @RabbitListener(queues = RabbitTopologyNames.STOCK_QUEUE, ackMode = "MANUAL")
    public void consumir(
            StockDescontarCommand command,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            stockRetryTemplate.execute(context -> {
                productoService.reducirStockDeVenta(command);
                return null;
            });
            channel.basicAck(deliveryTag, false);
            log.info("Stock descontado para la venta {}", command.ventaId());
        } catch (ProductoNotFoundException | IllegalArgumentException e) {
            log.error("Comando no recuperable para la venta {}: {}", command.ventaId(), e.getMessage());
            channel.basicNack(deliveryTag, false, false);
            return;
        } catch (Exception e) {
            log.error("Reintentos agotados para la venta {}. El mensaje va a la DLQ.", command.ventaId(), e);
            channel.basicNack(deliveryTag, false, false);
            return;
        }
        publicarEvento(command);
    }

    private void publicarEvento(StockDescontarCommand command) {
        try {
            stockEventPublisher.publicar(eventoDe(command));
        } catch (Exception e) {
            log.error("El stock de la venta {} ya quedó descontado, pero el evento no se publicó.",
                    command.ventaId(), e);
        }
    }

    private static StockDescontadoEvent eventoDe(StockDescontarCommand command) {
        List<StockDescontadoEvent.Item> items = command.items().stream()
                .map(item -> new StockDescontadoEvent.Item(item.productoCode(), item.cantidad()))
                .toList();
        return new StockDescontadoEvent(command.ventaId(), items);
    }
}
