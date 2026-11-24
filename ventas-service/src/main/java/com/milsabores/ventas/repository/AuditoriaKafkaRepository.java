package com.milsabores.ventas.repository;

import com.milsabores.ventas.model.AuditoriaKafka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaKafkaRepository extends JpaRepository<AuditoriaKafka, Long> {

    boolean existsByTopicAndParticionAndKafkaOffset(String topic, int particion, long kafkaOffset);

    List<AuditoriaKafka> findTop50ByOrderByRecibidoEnDesc();

    List<AuditoriaKafka> findByVentaIdOrderByRecibidoEnAsc(Long ventaId);
}
