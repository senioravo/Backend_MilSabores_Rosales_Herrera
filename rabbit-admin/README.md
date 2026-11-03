# Microservicio administrador de RabbitMQ

Crea y borra colas, exchanges y bindings en el clúster. No consume mensajes. Puerto **8086**.

La lógica está en `RabbitAdminService`: valida el dato y solo entonces llama a `RabbitAdmin` (`declareQueue`, `deleteQueue`, `declareExchange`, `deleteExchange`, `declareBinding`, `removeBinding`). Nombre vacío, nombre que empieza por `amq.`, tipo de exchange distinto de direct, topic, fanout o headers, o routing key vacía: responde 400 y no crea nada. El mensaje es `Hay datos vacíos o inválidos. No se creó nada.`

El controlador es `RabbitAdminController`, bajo `/api/admin`.

| Método | Ruta | Respuesta |
|--------|------|-----------|
| POST | `/api/admin/colas` | 201 |
| DELETE | `/api/admin/colas/{nombre}` | 204 |
| POST | `/api/admin/exchanges` | 201 |
| DELETE | `/api/admin/exchanges/{nombre}` | 204 |
| POST | `/api/admin/bindings` | 201 |
| DELETE | `/api/admin/bindings` | 204 (el cuerpo va en el body) |

Quien entra desde afuera pasa por el gateway y por el BFF. El BFF exige el rol `ADMIN`. Este proceso, en el puerto 8086, no vuelve a mirar el JWT: queda en la red interna.

## Cómo correrlo

El clúster tiene que estar arriba.

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:RABBITMQ_ADDRESSES = "localhost:5672,localhost:5673"
.\gradlew.bat bootRun
```

| Variable | Default dentro de Compose |
|----------|---------------------------|
| `RABBITMQ_ADDRESSES` | `rabbit1:5672,rabbit2:5672` |
| `RABBITMQ_USER` | `admin` |
| `RABBITMQ_PASSWORD` | `admin123` |

`GET http://localhost:8086/actuator/health`.

## Pruebas

```powershell
.\gradlew.bat test
```

`RabbitAdminServiceTest` comprueba que un dato inválido no llama al broker. `RabbitAdminControllerTest` comprueba el 400 y el 201.
