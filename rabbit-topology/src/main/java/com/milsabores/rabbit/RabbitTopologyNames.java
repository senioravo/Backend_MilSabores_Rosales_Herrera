package com.milsabores.rabbit;

/**
 * Nombres de la topología RabbitMQ de Mil Sabores.
 * Esta clase no declara colas ni publica mensajes: solo guarda los textos
 * que después usan los beans de configuración y los microservicios.
 */
public final class RabbitTopologyNames {

    /** Comando de descuento de stock. Un solo consumidor, clave exacta. */
    public static final String DIRECT_EXCHANGE = "milsabores.direct";

    /** Eventos que escuchan notificación y auditoría, cada una con su patrón. */
    public static final String TOPIC_EXCHANGE = "milsabores.topic";

    /**
     * Exchange direct al que caen los mensajes muertos.
     * La routing key es el nombre de la DLQ, para que cada falla llegue a la suya.
     */
    public static final String DLX_EXCHANGE = "milsabores.dlx";

    public static final String STOCK_ROUTING_KEY = "pedido.stock.descontar";
    public static final String STOCK_QUEUE = "pedido.stock";
    public static final String STOCK_DLQ = "pedido.stock.dlq";

    public static final String STOCK_EVENT_ROUTING_KEY = "pedido.stock.descontado";

    /** Una palabra después de pedido.stock. Recibe pedido.stock.descontado. */
    public static final String NOTIFICATION_BINDING = "pedido.stock.*";
    public static final String NOTIFICATION_QUEUE = "pedido.notificacion";
    public static final String NOTIFICATION_DLQ = "pedido.notificacion.dlq";

    /** pedido y cualquier cosa después. También recibe pedido.stock.descontado. */
    public static final String AUDIT_BINDING = "pedido.#";
    public static final String AUDIT_QUEUE = "pedido.auditoria";
    public static final String AUDIT_DLQ = "pedido.auditoria.dlq";

    private RabbitTopologyNames() {
    }
}
