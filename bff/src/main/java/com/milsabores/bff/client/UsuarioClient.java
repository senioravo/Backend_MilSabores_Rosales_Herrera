package com.milsabores.bff.client;

import com.milsabores.bff.dto.UsuarioDTO;
import com.milsabores.bff.exception.ServicioNoDisponibleException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UsuarioClient {

    private final RestClient restClient;

    public UsuarioClient(@Qualifier("usuarioRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @CircuitBreaker(name = "usuario-service", fallbackMethod = "obtenerPorIdFallback")
    @Retry(name = "usuario-service")
    public UsuarioDTO obtenerPorId(Long usuarioId) {
        return restClient.get()
                .uri("/api/usuarios/{id}", usuarioId)
                .retrieve()
                .body(UsuarioDTO.class);
    }

    private UsuarioDTO obtenerPorIdFallback(Long usuarioId, Throwable t) {
        throw new ServicioNoDisponibleException("usuario-service", t);
    }
}
