package com.milsabores.usuario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Usuario Service API - Mil Sabores")
                        .version("1.0")
                        .description("API REST para la gestión de usuarios y autenticación del sistema Mil Sabores")
                        .contact(new Contact()
                                .name("Mil Sabores Team")
                                .email("contacto@milsabores.cl")));
    }
}
