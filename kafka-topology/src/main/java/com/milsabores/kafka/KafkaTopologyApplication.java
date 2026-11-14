package com.milsabores.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.List;
import java.util.Map;

/**
 * Arranca solo para crear los dos topics y se cierra.
 * No atiende ventas ni stock.
 */
@SpringBootApplication
public class KafkaTopologyApplication {

    private static final Logger log = LoggerFactory.getLogger(KafkaTopologyApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(KafkaTopologyApplication.class, args);
        System.exit(SpringApplication.exit(context));
    }

    @Bean
    CommandLineRunner showTopics(KafkaAdmin kafkaAdmin) {
        return args -> {
            try (AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
                Map<String, TopicDescription> topics = client.describeTopics(List.of(
                                KafkaTopologyNames.STOCK_TOPIC,
                                KafkaTopologyNames.VENTAS_TOPIC))
                        .allTopicNames()
                        .get();
                topics.forEach((name, description) -> log.info(
                        "Topic {} listo: {} particiones, {} réplicas",
                        name,
                        description.partitions().size(),
                        description.partitions().get(0).replicas().size()));
            }
        };
    }
}
