package com.milsabores.producto.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Producto Service API - Mil Sabores")
                        .version("1.0")
                        .description("API REST para la gestión de productos y categorías del sistema Mil Sabores")
                        .contact(new Contact()
                                .name("Mil Sabores Team")
                                .email("contacto@milsabores.cl")));
    }
    
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/**")
                .packagesToScan("com.milsabores.producto.controller")
                .build();
    }
}
