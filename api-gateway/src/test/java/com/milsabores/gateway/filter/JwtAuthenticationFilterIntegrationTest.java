package com.milsabores.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Guía EP1: el gateway rechaza peticiones sin JWT válido en rutas protegidas.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class JwtAuthenticationFilterIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void protectedRouteWithoutBearerReturns401() {
        webTestClient
                .get()
                .uri("/api/usuarios/me")
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .jsonPath("$.error")
                .isNotEmpty();
    }

    @Test
    void protectedRouteWithInvalidBearerReturns401() {
        webTestClient
                .get()
                .uri("/api/carritos/usuario/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void optionsPreflightDoesNotRequireBearer() {
        webTestClient
                .options()
                .uri("/api/usuarios/me")
                .exchange()
                .expectStatus()
                .value(status -> assertNotEquals(401, status, "OPTIONS no debe responder 401 por falta de JWT"));
    }
}
