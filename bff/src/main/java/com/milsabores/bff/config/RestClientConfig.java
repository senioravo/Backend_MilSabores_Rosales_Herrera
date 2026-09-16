package com.milsabores.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient usuarioRestClient(@Value("${services.usuario.url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient productoRestClient(@Value("${services.producto.url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient carritoRestClient(@Value("${services.carrito.url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient ventaRestClient(@Value("${services.ventas.url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
