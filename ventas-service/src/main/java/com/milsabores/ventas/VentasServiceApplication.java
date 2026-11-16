package com.milsabores.ventas;

import com.milsabores.kafka.KafkaConsumerErrorConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(KafkaConsumerErrorConfig.class)
@SpringBootApplication
public class VentasServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VentasServiceApplication.class, args);
    }

}
