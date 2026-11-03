# Microservicio de catálogo — productos

Catálogo de productos y categorías, y el único proceso que descuenta stock. Puerto **8082**. Persiste en Neon con JPA (`Producto`, `Categoria` y sus repositorios). La conexión es `DATABASE_URL`, `DATABASE_USERNAME` y `DATABASE_PASSWORD`.

## Qué hace con RabbitMQ

Los listeners viven en el mismo proceso, agrupados por dominio. El ACK es manual.

| Paquete | Cola | Qué hace |
|---------|------|----------|
| `producto.messaging` | `pedido.stock` | Descuenta el stock. ACK solo si quedó hecho. Si no se recupera, NACK sin requeue y el mensaje va a `pedido.stock.dlq`. Después del ACK publica `pedido.stock.descontado` en el exchange topic `milsabores.topic`. |
| `notificacion` | `pedido.notificacion` | Binding `pedido.stock.*`. Deja el aviso en el log y hace ACK. |
| `auditoria` | `pedido.auditoria` | Binding `pedido.#`. Deja el registro en el log y hace ACK. |
| los tres `*DlqListener` | cada `*.dlq` | Escribe en el log el mensaje que no se pudo procesar y hace ACK. |

La configuración del conversor, del reintento (3 veces, solo errores de base de datos) y del ACK manual está en `config/ProductoRabbitConfig.java`. Los nombres de colas, exchanges y bindings están en el módulo `rabbit-topology`, clase `RabbitTopologyNames`. Los beans `Queue`, `Exchange` y `Binding` están en `RabbitTopologyConfig`.

Un mensaje en `pedido.stock` con clave exacta `pedido.stock.descontar` es el direct. El evento `pedido.stock.descontado` lo reciben las dos colas del topic: `*` calza una palabra y `#` calza el resto.

## Cómo correrlo

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:RABBITMQ_ADDRESSES = "localhost:5672,localhost:5673"
.\gradlew.bat bootRun
```

Hace falta Neon y el clúster (`localhost:5672` y `localhost:5673` desde el PC). `GET http://localhost:8082/actuator/health`. Swagger: `http://localhost:8082/swagger-ui.html`.

Con este proceso arriba, cada una de las seis colas tiene un consumidor. **Ready** en la Management UI son los mensajes que todavía esperan. El Ready de cada `*.dlq` es lo que no se pudo procesar.
