# Microservicio principal — ventas

Dueño de la venta y del pago. Puerto **8084**. Persiste en Neon PostgreSQL con JPA: entidad `Venta`, repositorio `VentaRepository`, conexión por `DATABASE_URL`, `DATABASE_USERNAME` y `DATABASE_PASSWORD` en `src/main/resources/application.properties`.

## Qué hace

- Crea la venta y su detalle, y cambia el estado.
- Inicia y confirma el pago con Transbank Webpay Plus (ambiente de integración).
- Cuando la venta pasa a `COMPLETADA` (pago aprobado) y eso quedó escrito en la base, publica `StockDescontarCommand` al exchange direct `milsabores.direct` con la clave `pedido.stock.descontar`. El mensaje no lleva precios ni datos de pago. Si la transacción se revierte, si el pago se rechaza o si la venta ya estaba completada, no se publica. Si RabbitMQ no responde, se deja en el log y la venta no falla.

Eso está en `service/impl/VentaServiceImpl.java` (`afterCommit`) y en `messaging/StockCommandPublisher.java`. El conversor JSON está en `config/VentasRabbitConfig.java`. Los nombres del exchange y de la clave vienen de `rabbit-topology` (`RabbitTopologyNames`).

## Cómo correrlo

El clúster RabbitMQ tiene que estar arriba (`rabbit1` en el puerto 5672 del PC). Desde esta carpeta, con JDK 17 o 21:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:RABBITMQ_ADDRESSES = "localhost:5672,localhost:5673"
.\gradlew.bat bootRun
```

| Variable | Para qué |
|----------|----------|
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | Neon |
| `RABBITMQ_ADDRESSES` | En el PC: `localhost:5672,localhost:5673`. Dentro de Compose: `rabbit1:5672,rabbit2:5672` |
| `RABBITMQ_USER`, `RABBITMQ_PASSWORD` | Por defecto `admin` / `admin123` |

`GET http://localhost:8084/actuator/health`. Swagger: `http://localhost:8084/swagger-ui.html`.

El checkout de la pantalla llama a `POST /api/ventas` y después a `POST /api/ventas/{id}/pagar`, pasando por el gateway. El comando de stock no sale en ninguno de los dos: sale cuando Transbank devuelve al usuario (`/api/ventas/transbank/return`) con el pago aprobado y `confirmarPago` deja la venta en `COMPLETADA`.
