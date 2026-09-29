package com.milsabores.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.milsabores.bff.config.AzureEntraProperties;

@SpringBootApplication
@EnableConfigurationProperties(AzureEntraProperties.class)
public class BffApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffApplication.class, args);
    }
}
