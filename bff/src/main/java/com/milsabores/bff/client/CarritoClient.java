package com.milsabores.bff.client;

import com.milsabores.bff.dto.CarritoDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CarritoClient {

    private final RestClient restClient;

    public CarritoClient(@Qualifier("carritoRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public CarritoDTO obtenerCarrito(Long usuarioId) {
        return restClient.get()
                .uri("/api/carritos/usuario/{usuarioId}", usuarioId)
                .retrieve()
                .body(CarritoDTO.class);
    }
}
