package com.milsabores.bff.client;

import com.milsabores.bff.dto.CarritoDTO;
import com.milsabores.bff.exception.ServicioNoDisponibleException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CarritoClient {

    private final RestClient restClient;

    public CarritoClient(@Qualifier("carritoRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @CircuitBreaker(name = "carrito-service", fallbackMethod = "obtenerCarritoFallback")
    @Retry(name = "carrito-service")
    public CarritoDTO obtenerCarrito(Long usuarioId) {
        return restClient.get()
                .uri("/api/carritos/usuario/{usuarioId}", usuarioId)
                .retrieve()
                .body(CarritoDTO.class);
    }

    private CarritoDTO obtenerCarritoFallback(Long usuarioId, Throwable t) {
        throw new ServicioNoDisponibleException("carrito-service", t);
    }
}
