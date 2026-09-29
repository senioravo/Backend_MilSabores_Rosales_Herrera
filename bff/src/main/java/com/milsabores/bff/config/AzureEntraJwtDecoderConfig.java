package com.milsabores.bff.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@Configuration
@ConditionalOnProperty(name = "azure.entra.enabled", havingValue = "true")
public class AzureEntraJwtDecoderConfig {

    private static final Logger log = LoggerFactory.getLogger(AzureEntraJwtDecoderConfig.class);

    @Bean
    JwtDecoder entraJwtDecoder(AzureEntraProperties entra) {
        if (!entra.isConfigured()) {
            throw new IllegalStateException("azure.entra.enabled=true pero faltan tenant o audience");
        }
        log.info("BFF Entra JwtDecoder listo (issuer={})", entra.getIssuerUri());
        return EntraJwtValidatorFactory.buildDecoder(entra);
    }
}
