package com.milsabores.ventas.auditoria;

import com.milsabores.kafka.StockDescontado;
import com.milsabores.kafka.VentaRegistrada;
import com.milsabores.ventas.model.AuditoriaKafka;
import com.milsabores.ventas.repository.AuditoriaKafkaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reacción del grupo auditoria.
 * No se conecta a Kafka: el listener le entrega el aviso ya leído.
 * Guarda una fila para que el aviso se vea en la API y en la pantalla.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaKafkaService {

    private final AuditoriaKafkaRepository repository;

    @Transactional
    public void registrarStockDescontado(StockDescontado aviso, String topic, int particion, long offset) {
        String productos = aviso.items().stream()
                .map(item -> item.productoCode() + " x" + item.cantidad())
                .collect(Collectors.joining(", "));
        guardar(
                aviso.ventaId(),
                "STOCK_DESCONTADO",
                "Venta " + aviso.ventaId() + " con stock descontado (" + productos + ")",
                topic,
                particion,
                offset);
    }

    @Transactional
    public void registrarVenta(VentaRegistrada aviso, String topic, int particion, long offset) {
        guardar(
                aviso.ventaId(),
                "VENTA_REGISTRADA",
                "Venta " + aviso.ventaId() + " de " + aviso.usuarioEmail()
                        + " en " + aviso.estado() + " por " + aviso.total(),
                topic,
                particion,
                offset);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaKafkaResponse> listar(Long ventaId) {
        List<AuditoriaKafka> filas = ventaId == null
                ? repository.findTop50ByOrderByRecibidoEnDesc()
                : repository.findByVentaIdOrderByRecibidoEnAsc(ventaId);
        return filas.stream().map(this::toResponse).toList();
    }

    private void guardar(Long ventaId, String tipo, String resumen, String topic, int particion, long offset) {
        if (repository.existsByTopicAndParticionAndKafkaOffset(topic, particion, offset)) {
            log.info("Auditoría ya registrada en {} partición {} offset {}", topic, particion, offset);
            return;
        }
        AuditoriaKafka fila = new AuditoriaKafka();
        fila.setVentaId(ventaId);
        fila.setTipo(tipo);
        fila.setResumen(resumen);
        fila.setTopic(topic);
        fila.setParticion(particion);
        fila.setKafkaOffset(offset);
        repository.save(fila);
        log.info("Auditoría guardada: {}", resumen);
    }

    private AuditoriaKafkaResponse toResponse(AuditoriaKafka fila) {
        return new AuditoriaKafkaResponse(
                fila.getId(),
                fila.getVentaId(),
                fila.getTipo(),
                fila.getResumen(),
                fila.getTopic(),
                fila.getParticion(),
                fila.getKafkaOffset(),
                fila.getRecibidoEn());
    }
}
