package com.milsabores.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declara los dos topics en el clúster.
 * No contiene la lógica de la venta ni del stock.
 */
@Configuration
public class KafkaTopologyConfig {

    @Bean
    public NewTopic stockTopic() {
        return topic(
                KafkaTopologyNames.STOCK_TOPIC,
                KafkaTopologyNames.STOCK_RETENTION_MS);
    }

    @Bean
    public NewTopic ventasTopic() {
        return topic(
                KafkaTopologyNames.VENTAS_TOPIC,
                KafkaTopologyNames.VENTAS_RETENTION_MS);
    }

    private static NewTopic topic(String name, String retentionMs) {
        return TopicBuilder.name(name)
                .partitions(KafkaTopologyNames.PARTITIONS)
                .replicas(KafkaTopologyNames.REPLICATION_FACTOR)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.RETENTION_MS_CONFIG, retentionMs)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, KafkaTopologyNames.MIN_IN_SYNC_REPLICAS)
                .build();
    }
}
