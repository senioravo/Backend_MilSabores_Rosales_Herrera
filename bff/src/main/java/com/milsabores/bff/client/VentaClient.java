package com.milsabores.bff.client;

import com.milsabores.bff.dto.TransbankResponseDTO;
import com.milsabores.bff.dto.VentaRequestDTO;
import com.milsabores.bff.dto.VentaResponseDTO;
import com.milsabores.bff.exception.ServicioNoDisponibleException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Sin @Retry: crearVenta y iniciarPago son POST no idempotentes (crean una
 * venta / inician una transaccion Transbank). Reintentarlos automaticamente
 * podria duplicar la venta o la transaccion de pago si la primera llamada
 * si llego a procesarse downstream. El circuit breaker si aplica: solo
 * evita seguir insistiendo cuando ventas-service ya esta caido.
 */
@Component
public class VentaClient {

    private final RestClient restClient;

    public VentaClient(@Qualifier("ventaRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @CircuitBreaker(name = "ventas-service", fallbackMethod = "crearVentaFallback")
    public VentaResponseDTO crearVenta(VentaRequestDTO ventaRequest) {
        return restClient.post()
                .uri("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ventaRequest)
                .retrieve()
                .body(VentaResponseDTO.class);
    }

    @CircuitBreaker(name = "ventas-service", fallbackMethod = "iniciarPagoFallback")
    public TransbankResponseDTO iniciarPago(Long ventaId) {
        return restClient.post()
                .uri("/api/ventas/{id}/pagar", ventaId)
                .retrieve()
                .body(TransbankResponseDTO.class);
    }

    private VentaResponseDTO crearVentaFallback(VentaRequestDTO ventaRequest, Throwable t) {
        throw new ServicioNoDisponibleException("ventas-service", t);
    }

    private TransbankResponseDTO iniciarPagoFallback(Long ventaId, Throwable t) {
        throw new ServicioNoDisponibleException("ventas-service", t);
    }
}
