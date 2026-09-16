package com.milsabores.bff.client;

import com.milsabores.bff.dto.TransbankResponseDTO;
import com.milsabores.bff.dto.VentaRequestDTO;
import com.milsabores.bff.dto.VentaResponseDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class VentaClient {

    private final RestClient restClient;

    public VentaClient(@Qualifier("ventaRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public VentaResponseDTO crearVenta(VentaRequestDTO ventaRequest) {
        return restClient.post()
                .uri("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ventaRequest)
                .retrieve()
                .body(VentaResponseDTO.class);
    }

    public TransbankResponseDTO iniciarPago(Long ventaId) {
        return restClient.post()
                .uri("/api/ventas/{id}/pagar", ventaId)
                .retrieve()
                .body(TransbankResponseDTO.class);
    }
}
