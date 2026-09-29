package com.milsabores.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

/**
 * Paso B2: decoder JWT reactivo para access tokens MSAL (issuer + JWKS Microsoft + audience).
 * El {@link com.milsabores.gateway.filter.JwtAuthenticationFilter} lo usará en B3.
 */
@Configuration
@ConditionalOnProperty(name = "azure.entra.enabled", havingValue = "true")
public class AzureEntraJwtDecoderConfig {

    private static final Logger log = LoggerFactory.getLogger(AzureEntraJwtDecoderConfig.class);

    @Bean
    ReactiveJwtDecoder entraReactiveJwtDecoder(AzureEntraProperties entra) {
        if (!entra.isConfigured()) {
            throw new IllegalStateException(
                    "azure.entra.enabled=true pero faltan AZURE_TENANT_ID o AZURE_API_AUDIENCE");
        }

        return EntraJwtValidatorFactory.buildDecoder(entra);
    }
}
