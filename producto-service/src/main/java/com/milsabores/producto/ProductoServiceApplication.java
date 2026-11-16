package com.milsabores.producto;

import com.milsabores.kafka.KafkaConsumerErrorConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(KafkaConsumerErrorConfig.class)
@SpringBootApplication(scanBasePackages = {
		"com.milsabores.producto",
		"com.milsabores.notificacion",
		"com.milsabores.auditoria"
})
public class ProductoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductoServiceApplication.class, args);
	}

}
