package com.milsabores.producto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
