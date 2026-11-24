package com.milsabores.ventas.auditoria;

import com.milsabores.kafka.StockDescontado;
import com.milsabores.ventas.model.AuditoriaKafka;
import com.milsabores.ventas.repository.AuditoriaKafkaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaKafkaServiceTest {

    @Mock
    private AuditoriaKafkaRepository repository;

    @InjectMocks
    private AuditoriaKafkaService service;

    @Test
    void guardaElStockLaPrimeraVez() {
        when(repository.existsByTopicAndParticionAndKafkaOffset("milsabores.stock", 2, 7L)).thenReturn(false);
        StockDescontado aviso = new StockDescontado(5L, List.of(new StockDescontado.Item("TC001", 1)));

        service.registrarStockDescontado(aviso, "milsabores.stock", 2, 7L);

        ArgumentCaptor<AuditoriaKafka> captor = ArgumentCaptor.forClass(AuditoriaKafka.class);
        verify(repository).save(captor.capture());
        assertEquals(5L, captor.getValue().getVentaId());
        assertEquals("STOCK_DESCONTADO", captor.getValue().getTipo());
        assertEquals(7L, captor.getValue().getKafkaOffset());
    }

    @Test
    void noDuplicaElMismoOffset() {
        when(repository.existsByTopicAndParticionAndKafkaOffset("milsabores.stock", 2, 7L)).thenReturn(true);
        StockDescontado aviso = new StockDescontado(5L, List.of(new StockDescontado.Item("TC001", 1)));

        service.registrarStockDescontado(aviso, "milsabores.stock", 2, 7L);

        verify(repository, never()).save(any());
    }
}
