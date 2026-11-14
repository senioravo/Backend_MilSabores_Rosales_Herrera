package com.milsabores.kafka;

/**
 * Nombres y números de los dos topics de Mil Sabores.
 * Esta clase no crea topics ni publica avisos: solo guarda los textos
 * que después usan el inicializador y los microservicios.
 */
public final class KafkaTopologyNames {

    /** Aviso de que el stock ya se descontó. Lo publica productos. */
    public static final String STOCK_TOPIC = "milsabores.stock";

    /** Aviso de que la venta quedó guardada. Lo publica ventas. */
    public static final String VENTAS_TOPIC = "milsabores.ventas";

    public static final int PARTITIONS = 3;

    public static final short REPLICATION_FACTOR = 3;

    /** El aviso de stock se conserva una hora y después Kafka lo borra. */
    public static final String STOCK_RETENTION_MS = "3600000";

    /** El aviso de la venta se conserva siete días, para poder volver a leerlo. */
    public static final String VENTAS_RETENTION_MS = "604800000";

    /** Una escritura se acepta solo si quedó en dos réplicas. */
    public static final String MIN_IN_SYNC_REPLICAS = "2";

    private KafkaTopologyNames() {
    }
}
