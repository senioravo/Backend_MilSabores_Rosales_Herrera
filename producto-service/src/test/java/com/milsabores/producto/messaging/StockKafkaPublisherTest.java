package com.milsabores.producto.messaging;

import com.milsabores.kafka.KafkaTopologyNames;
import com.milsabores.kafka.StockDescontado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockKafkaPublisherTest {

    @Mock
    private KafkaTemplate<String, StockDescontado> kafkaTemplate;

    @Test
    void publicaEnElTopicDeStockConLaVentaComoClave() throws Exception {
        when(kafkaTemplate.send(eq(KafkaTopologyNames.STOCK_TOPIC), eq("5"), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
        StockKafkaPublisher publisher = new StockKafkaPublisher(kafkaTemplate);
        StockDescontado aviso = new StockDescontado(5L, List.of(new StockDescontado.Item("TC001", 1)));

        publisher.publicar(aviso);

        ArgumentCaptor<StockDescontado> avisoEnviado = ArgumentCaptor.forClass(StockDescontado.class);
        verify(kafkaTemplate).send(eq(KafkaTopologyNames.STOCK_TOPIC), eq("5"), avisoEnviado.capture());
        assertEquals("TC001", avisoEnviado.getValue().items().get(0).productoCode());
    }
}
