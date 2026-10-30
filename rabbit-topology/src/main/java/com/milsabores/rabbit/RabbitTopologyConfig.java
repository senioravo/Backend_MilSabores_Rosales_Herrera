package com.milsabores.rabbit;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara exchanges, colas y bindings en el clúster.
 * No contiene la lógica de la venta ni del stock.
 */
@Configuration
public class RabbitTopologyConfig {

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    /** Si nadie consume a tiempo, el mensaje muere y pasa a su DLQ. */
    static final int MAIN_MESSAGE_TTL_MS = 60_000;

    /** Tope de mensajes en la cola de trabajo. El más viejo pasa a la DLQ. */
    static final int MAIN_MAX_LENGTH = 100;

    /** La cuarentena se conserva un día y después se descarta. */
    static final int DLQ_MESSAGE_TTL_MS = 86_400_000;

    static final int DLQ_MAX_LENGTH = 200;

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(RabbitTopologyNames.DIRECT_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(RabbitTopologyNames.TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(RabbitTopologyNames.DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue stockQueue() {
        return workQueue(RabbitTopologyNames.STOCK_QUEUE, RabbitTopologyNames.STOCK_DLQ);
    }

    @Bean
    public Queue notificationQueue() {
        return workQueue(RabbitTopologyNames.NOTIFICATION_QUEUE, RabbitTopologyNames.NOTIFICATION_DLQ);
    }

    @Bean
    public Queue auditQueue() {
        return workQueue(RabbitTopologyNames.AUDIT_QUEUE, RabbitTopologyNames.AUDIT_DLQ);
    }

    @Bean
    public Queue stockDlq() {
        return deadLetterQueue(RabbitTopologyNames.STOCK_DLQ);
    }

    @Bean
    public Queue notificationDlq() {
        return deadLetterQueue(RabbitTopologyNames.NOTIFICATION_DLQ);
    }

    @Bean
    public Queue auditDlq() {
        return deadLetterQueue(RabbitTopologyNames.AUDIT_DLQ);
    }

    @Bean
    public Binding stockBinding(
            @Qualifier("stockQueue") Queue stockQueue,
            @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(stockQueue)
                .to(directExchange)
                .with(RabbitTopologyNames.STOCK_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding(
            @Qualifier("notificationQueue") Queue notificationQueue,
            @Qualifier("topicExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(topicExchange)
                .with(RabbitTopologyNames.NOTIFICATION_BINDING);
    }

    @Bean
    public Binding auditBinding(
            @Qualifier("auditQueue") Queue auditQueue,
            @Qualifier("topicExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(auditQueue)
                .to(topicExchange)
                .with(RabbitTopologyNames.AUDIT_BINDING);
    }

    @Bean
    public Binding stockDlqBinding(
            @Qualifier("stockDlq") Queue stockDlq,
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(stockDlq)
                .to(deadLetterExchange)
                .with(RabbitTopologyNames.STOCK_DLQ);
    }

    @Bean
    public Binding notificationDlqBinding(
            @Qualifier("notificationDlq") Queue notificationDlq,
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(notificationDlq)
                .to(deadLetterExchange)
                .with(RabbitTopologyNames.NOTIFICATION_DLQ);
    }

    @Bean
    public Binding auditDlqBinding(
            @Qualifier("auditDlq") Queue auditDlq,
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(auditDlq)
                .to(deadLetterExchange)
                .with(RabbitTopologyNames.AUDIT_DLQ);
    }

    private static Queue workQueue(String name, String deadLetterRoutingKey) {
        return QueueBuilder.durable(name)
                .ttl(MAIN_MESSAGE_TTL_MS)
                .maxLength(MAIN_MAX_LENGTH)
                .deadLetterExchange(RabbitTopologyNames.DLX_EXCHANGE)
                .deadLetterRoutingKey(deadLetterRoutingKey)
                .build();
    }

    private static Queue deadLetterQueue(String name) {
        return QueueBuilder.durable(name)
                .ttl(DLQ_MESSAGE_TTL_MS)
                .maxLength(DLQ_MAX_LENGTH)
                .build();
    }
}
