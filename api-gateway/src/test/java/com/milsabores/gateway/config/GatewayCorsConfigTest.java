package com.milsabores.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayCorsConfigTest {

    @Test
    void corsAllowsLocalDevOriginAndAuthorizationHeader() {
        GatewayCorsConfig config = new GatewayCorsConfig();
        CorsConfigurationSource source = config.corsConfigurationSource("https://example.vercel.app");

        MockServerWebExchange exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/api/productos").build());

        CorsConfiguration cors = source.getCorsConfiguration(exchange);
        assertNotNull(cors);
        assertTrue(cors.getAllowedOrigins().contains("http://localhost:5173"));
        assertTrue(cors.getAllowedOrigins().contains("https://example.vercel.app"));
        assertTrue(cors.getAllowedMethods().contains("OPTIONS"));
        assertTrue(cors.getAllowedHeaders().contains("Authorization"));
    }
}
