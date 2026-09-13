package com.milsabores.ventas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TransbankConfig {
    
    @Value("${transbank.api.url}")
    private String apiUrl;
    
    @Value("${transbank.api.key-id}")
    private String keyId;
    
    @Value("${transbank.api.key-secret}")
    private String keySecret;
    
    @Bean
    public WebClient transbankWebClient() {
        return WebClient.builder()
            .baseUrl(apiUrl)
            .defaultHeader("Tbk-Api-Key-Id", keyId)
            .defaultHeader("Tbk-Api-Key-Secret", keySecret)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }
    
    public String getKeyId() {
        return keyId;
    }
    
    public String getKeySecret() {
        return keySecret;
    }
}
