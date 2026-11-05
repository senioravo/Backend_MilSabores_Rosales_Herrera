package com.milsabores.producto.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class ProductoRabbitConfig {

    static final int MAX_INTENTOS = 3;

    /** Nombre del conversor que usan los listeners de las DLQ. */
    public static final String DLQ_MESSAGE_CONVERTER = "dlqMessageConverter";

    /** Conversor por defecto (RabbitTemplate y consumidores de trabajo). */
    @Bean
    @Primary
    public MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.milsabores.rabbit");
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    /**
     * Las DLQ reciben cualquier cuerpo, también JSON ilegible. Sus listeners solo
     * leen los bytes para el log, así que no deben convertir el mensaje: si lo
     * hicieran, un cuerpo inválido fallaría antes del ACK y bloquearía la DLQ.
     */
    @Bean(DLQ_MESSAGE_CONVERTER)
    public MessageConverter dlqMessageConverter() {
        return new SimpleMessageConverter();
    }

    @Bean
    public RetryTemplate stockRetryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(MAX_INTENTOS)
                .exponentialBackoff(1000, 2, 8000)
                .retryOn(DataAccessException.class)
                .build();
    }
}
