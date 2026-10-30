package com.milsabores.rabbit;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Arranca solo para que Spring declare la topología en el clúster y se cierra.
 * No atiende ventas ni stock.
 */
@SpringBootApplication
public class RabbitTopologyApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(RabbitTopologyApplication.class, args);
        System.exit(SpringApplication.exit(context));
    }

    @Bean
    CommandLineRunner declareTopology(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }
}
