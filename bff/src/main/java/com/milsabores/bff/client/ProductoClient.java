package com.milsabores.bff.client;

import com.milsabores.bff.dto.ProductoDTO;
import com.milsabores.bff.exception.ServicioNoDisponibleException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductoClient {

    private final RestClient restClient;

    public ProductoClient(@Qualifier("productoRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @CircuitBreaker(name = "producto-service", fallbackMethod = "obtenerPorCodeFallback")
    @Retry(name = "producto-service")
    public ProductoDTO obtenerPorCode(String productoCode) {
        return restClient.get()
                .uri("/api/productos/{code}", productoCode)
                .retrieve()
                .body(ProductoDTO.class);
    }

    private ProductoDTO obtenerPorCodeFallback(String productoCode, Throwable t) {
        throw new ServicioNoDisponibleException("producto-service", t);
    }
}
