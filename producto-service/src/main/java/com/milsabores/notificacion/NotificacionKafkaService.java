package com.milsabores.notificacion;

import com.milsabores.kafka.StockDescontado;
import com.milsabores.kafka.VentaRegistrada;
import com.milsabores.producto.model.NotificacionKafka;
import com.milsabores.producto.repository.NotificacionKafkaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reacción del grupo notificacion.
 * No se conecta a Kafka: el listener le entrega el aviso ya leído.
 * Guarda una fila para que el aviso se vea en la API y en la pantalla.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionKafkaService {

    private final NotificacionKafkaRepository repository;

    @Transactional
    public void avisarStockDescontado(StockDescontado aviso, String topic, int particion, long offset) {
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
    public void avisarVentaRegistrada(VentaRegistrada aviso, String topic, int particion, long offset) {
        guardar(
                aviso.ventaId(),
                "VENTA_REGISTRADA",
                "Venta " + aviso.ventaId() + " de " + aviso.usuarioEmail()
                        + " quedó en " + aviso.estado() + " por " + aviso.total(),
                topic,
                particion,
                offset);
    }

    @Transactional(readOnly = true)
    public List<NotificacionKafkaResponse> listar(Long ventaId) {
        List<NotificacionKafka> filas = ventaId == null
                ? repository.findTop50ByOrderByRecibidoEnDesc()
                : repository.findByVentaIdOrderByRecibidoEnAsc(ventaId);
        return filas.stream().map(this::toResponse).toList();
    }

    private void guardar(Long ventaId, String tipo, String resumen, String topic, int particion, long offset) {
        if (repository.existsByTopicAndParticionAndKafkaOffset(topic, particion, offset)) {
            log.info("Notificación ya registrada en {} partición {} offset {}", topic, particion, offset);
            return;
        }
        NotificacionKafka fila = new NotificacionKafka();
        fila.setVentaId(ventaId);
        fila.setTipo(tipo);
        fila.setResumen(resumen);
        fila.setTopic(topic);
        fila.setParticion(particion);
        fila.setKafkaOffset(offset);
        repository.save(fila);
        log.info("Notificación guardada: {}", resumen);
    }

    private NotificacionKafkaResponse toResponse(NotificacionKafka fila) {
        return new NotificacionKafkaResponse(
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
