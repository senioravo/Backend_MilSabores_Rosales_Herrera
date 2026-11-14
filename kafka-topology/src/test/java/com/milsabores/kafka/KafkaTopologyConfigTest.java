package com.milsabores.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaTopologyConfigTest {

    private final KafkaTopologyConfig config = new KafkaTopologyConfig();

    @Test
    void stockTieneTresCopiasYSeBorraEnUnaHora() {
        NewTopic topic = config.stockTopic();

        assertEquals(KafkaTopologyNames.STOCK_TOPIC, topic.name());
        assertEquals(3, topic.numPartitions());
        assertEquals((short) 3, topic.replicationFactor());
        assertEquals("3600000", topic.configs().get(TopicConfig.RETENTION_MS_CONFIG));
        assertEquals(TopicConfig.CLEANUP_POLICY_DELETE, topic.configs().get(TopicConfig.CLEANUP_POLICY_CONFIG));
    }

    @Test
    void ventasTieneTresCopiasYSeConservaSieteDias() {
        NewTopic topic = config.ventasTopic();

        assertEquals(KafkaTopologyNames.VENTAS_TOPIC, topic.name());
        assertEquals(3, topic.numPartitions());
        assertEquals((short) 3, topic.replicationFactor());
        assertEquals("604800000", topic.configs().get(TopicConfig.RETENTION_MS_CONFIG));
        assertEquals("2", topic.configs().get(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG));
    }
}
