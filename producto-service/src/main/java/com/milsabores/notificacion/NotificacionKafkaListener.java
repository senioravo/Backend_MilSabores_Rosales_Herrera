package com.milsabores.notificacion;

import com.milsabores.kafka.AvisoKafkaValidador;
import com.milsabores.kafka.KafkaTopologyNames;
import com.milsabores.kafka.StockDescontado;
import com.milsabores.kafka.VentaRegistrada;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Grupo notificacion. Lee milsabores.stock y milsabores.ventas.
 * Valida el aviso y llama al servicio. Si el aviso es ilegible, el manejador
 * de errores lo anota y este grupo sigue. Un fallo de base se reintenta.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
        id = "notificacion",
        topics = {KafkaTopologyNames.STOCK_TOPIC, KafkaTopologyNames.VENTAS_TOPIC},
        groupId = "${milsabores.kafka.group.notificacion}")
public class NotificacionKafkaListener {

    private final NotificacionKafkaService notificacionKafkaService;

    @KafkaHandler
    public void recibirStock(@Payload StockDescontado aviso, ConsumerRecord<String, StockDescontado> registro) {
        AvisoKafkaValidador.validarStock(aviso);
        log.info("Grupo notificacion leyó {} partición {} offset {} venta {}",
                registro.topic(), registro.partition(), registro.offset(), aviso.ventaId());
        notificacionKafkaService.avisarStockDescontado(
                aviso, registro.topic(), registro.partition(), registro.offset());
    }

    @KafkaHandler
    public void recibirVenta(@Payload VentaRegistrada aviso, ConsumerRecord<String, VentaRegistrada> registro) {
        AvisoKafkaValidador.validarVenta(aviso);
        log.info("Grupo notificacion leyó {} partición {} offset {} venta {}",
                registro.topic(), registro.partition(), registro.offset(), aviso.ventaId());
        notificacionKafkaService.avisarVentaRegistrada(
                aviso, registro.topic(), registro.partition(), registro.offset());
    }

    @KafkaHandler(isDefault = true)
    public void recibirDesconocido(@Payload Object aviso, ConsumerRecord<?, ?> registro) {
        String tipo = aviso == null ? "vacío" : aviso.getClass().getName();
        throw new IllegalArgumentException("Aviso no reconocido (" + tipo + ") en " + registro.topic()
                + " partición " + registro.partition() + " offset " + registro.offset());
    }
}
