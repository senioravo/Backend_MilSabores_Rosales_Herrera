# BFF — Mil Sabores

Backend for Frontend. El navegador habla con el API Gateway (puerto 8080) y el gateway reenvía aquí, al puerto **8085**. Este servicio no tiene base de datos: junta usuario, carrito, catálogo y ventas en una sola respuesta.

## Qué hace

- `GET /bff/checkout/resumen` arma el resumen del carrito con precio y stock verificados.
- `POST /bff/checkout` crea la venta e inicia el pago con Transbank. No espera a que RabbitMQ descuente el stock.
- Vuelve a validar el JWT que ya revisó el gateway. El filtro es `BffJwtAuthenticationFilter`.
- `/api/admin/**` solo pasa si el token trae el rol `ADMIN`. Sin token responde 401. Con un rol distinto responde 403, con el texto `Se requiere el rol ADMIN`. Esa ruta se reenvía a `rabbit-admin`.

El código de la seguridad está en `src/main/java/com/milsabores/bff/security/` y `config/BffSecurityConfig.java`. El reenvío está en `proxy/DownstreamApiProxy.java`.

## Cómo correrlo

Desde esta carpeta, con `JAVA_HOME` en JDK 17 o 21 (Gradle 8.11 no arranca en JDK 25):

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\gradlew.bat bootRun
```

Hace falta que usuario (8081), producto (8082), carrito (8083), ventas (8084) y, si se usa el administrador, rabbit-admin (8086) estén arriba. Las URLs salen de estas variables, con ese localhost por defecto:

| Variable | Default |
|----------|---------|
| `USUARIO_SERVICE_URL` | `http://localhost:8081` |
| `PRODUCTO_SERVICE_URL` | `http://localhost:8082` |
| `CARRITO_SERVICE_URL` | `http://localhost:8083` |
| `VENTAS_SERVICE_URL` | `http://localhost:8084` |
| `RABBIT_ADMIN_SERVICE_URL` | `http://localhost:8086` |

La comprobación de vida es `GET http://localhost:8085/actuator/health`. Swagger: `http://localhost:8085/swagger-ui.html`.

## Pruebas

```powershell
.\gradlew.bat test
```

`BffJwtValidatorTest` y `BffSecurityIntegrationTest` cubren token ausente, token malo, rol que no es admin y rol `ADMIN`.
