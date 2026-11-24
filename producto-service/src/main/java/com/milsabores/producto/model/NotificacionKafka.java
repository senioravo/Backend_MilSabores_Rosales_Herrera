package com.milsabores.producto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Fila que deja el grupo notificacion al leer un aviso.
 * La clave topic + partición + offset evita duplicar el mismo aviso si Kafka lo reentrega.
 */
@Entity
@Table(
        name = "notificaciones_kafka",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notificacion_kafka_posicion",
                columnNames = {"topic", "particion", "kafka_offset"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionKafka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venta_id", nullable = false)
    private Long ventaId;

    @Column(name = "tipo", nullable = false, length = 40)
    private String tipo;

    @Column(name = "resumen", nullable = false, length = 500)
    private String resumen;

    @Column(name = "topic", nullable = false, length = 249)
    private String topic;

    @Column(name = "particion", nullable = false)
    private int particion;

    @Column(name = "kafka_offset", nullable = false)
    private long kafkaOffset;

    @Column(name = "recibido_en", nullable = false)
    private LocalDateTime recibidoEn;

    @PrePersist
    public void prePersist() {
        if (recibidoEn == null) {
            recibidoEn = LocalDateTime.now();
        }
    }
}
