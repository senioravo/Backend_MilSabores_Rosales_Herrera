package com.milsabores.bff.client;

import com.milsabores.bff.dto.UsuarioDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UsuarioClient {

    private final RestClient restClient;

    public UsuarioClient(@Qualifier("usuarioRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public UsuarioDTO obtenerPorId(Long usuarioId) {
        return restClient.get()
                .uri("/api/usuarios/{id}", usuarioId)
                .retrieve()
                .body(UsuarioDTO.class);
    }
}
