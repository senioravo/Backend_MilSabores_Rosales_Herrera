package com.milsabores.producto.repository;

import com.milsabores.producto.model.NotificacionKafka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionKafkaRepository extends JpaRepository<NotificacionKafka, Long> {

    boolean existsByTopicAndParticionAndKafkaOffset(String topic, int particion, long kafkaOffset);

    List<NotificacionKafka> findTop50ByOrderByRecibidoEnDesc();

    List<NotificacionKafka> findByVentaIdOrderByRecibidoEnAsc(Long ventaId);
}
