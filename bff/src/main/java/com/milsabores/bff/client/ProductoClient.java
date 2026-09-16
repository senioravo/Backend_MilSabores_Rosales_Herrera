package com.milsabores.bff.client;

import com.milsabores.bff.dto.ProductoDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductoClient {

    private final RestClient restClient;

    public ProductoClient(@Qualifier("productoRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public ProductoDTO obtenerPorCode(String productoCode) {
        return restClient.get()
                .uri("/api/productos/{code}", productoCode)
                .retrieve()
                .body(ProductoDTO.class);
    }
}
