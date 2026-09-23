# Backend Mil Sabores - Arquitectura de Microservicios

Backend de la pastelería Mil Sabores, construido con Spring Boot como microservicios independientes. Todos comparten una base de datos Neon PostgreSQL, y el frontend (React 18 + Vite, en Vercel) accede a ellos a través de un API Gateway.

## 🔗 Repositorios del proyecto

| Componente | Repositorio |
|------------|-------------|
| **Frontend** | https://github.com/senioravo/DSY1104_ROSALES_HERRERA |
| **Backend** | https://github.com/senioravo/Backend_MilSabores_Rosales_Herrera |

## 🌿 Flujo de ramas

- `main`: código estable listo para entrega
- `develop`: integración del equipo
- `feature/*`: desarrollo por tarea (ej. `feature/setup-repos`)

## 🏗️ Arquitectura

```
                 Frontend (Vercel)
                        │
                        ▼
              ┌───────────────────┐
              │  API Gateway :8080│  JWT + rate limiting + CORS
              └─────────┬─────────┘
        ┌──────────┬────┴─────┬───────────┬──────────┐
        ▼          ▼          ▼           ▼          ▼
   usuario    producto    carrito     ventas       BFF :8085
    :8081      :8082       :8083       :8084     (checkout)
        └──────────┴──────────┴───────────┘          │
                        │                llama directo a los 4
                        ▼
              Neon PostgreSQL (compartida)
```

Cada microservicio sigue las capas Controller → Service → Repository → Entity/DTO y tiene su propio `GlobalExceptionHandler`. Los servicios se referencian solo por ID: no hay claves foráneas entre tablas de servicios distintos.

### API Gateway (Puerto 8080)
- Punto de entrada único (Spring Cloud Gateway)
- Enruta `/api/usuarios`, `/api/productos`, `/api/categorias`, `/api/carritos`, `/api/ventas` y `/bff`
- Valida el JWT emitido por usuario-service y reenvía `X-User-Id` y `X-User-Email` a los servicios
- Rutas públicas (sin token): login, registro, productos, categorías y `/actuator`
- Rate limiting por IP en memoria (60 peticiones/minuto por defecto)
- CORS para `localhost:5173`, `localhost:3000` y el frontend en Vercel (`FRONTEND_URL`)

### 1. Usuario Service (Puerto 8081)
- Registro y login de usuarios
- Contraseñas hasheadas con BCrypt
- Emisión de tokens JWT (expiran en 24 h)
- CRUD completo de usuarios

### 2. Producto Service (Puerto 8082)
- CRUD de productos y categorías
- Búsqueda por nombre, filtro por categoría y productos destacados
- Gestión de stock (actualizar y descontar)

### 3. Carrito Service (Puerto 8083)
- Carrito de compras por usuario
- Agregar y eliminar productos, actualizar cantidades
- Cálculo de totales y cantidad de ítems

### 4. Ventas Service (Puerto 8084)
- Ventas y detalle de ventas
- Pago con Transbank Webpay Plus (ambiente de integración)
- Estados de venta: `PENDIENTE`, `PROCESANDO`, `COMPLETADA`, `RECHAZADA`, `CANCELADA`
- Consultas por usuario, estado y rango de fechas

### BFF - Backend For Frontend (Puerto 8085)
- Orquesta el checkout: combina carrito, productos, usuario y ventas en una sola llamada
- Verifica precio y stock contra producto-service antes de crear la venta
- Circuit breaker (Resilience4j) hacia cada microservicio y reintentos solo en lecturas (GET), para no duplicar ventas ni pagos

## 🗄️ Base de Datos

**PostgreSQL en Neon**
- Todas las tablas están en el mismo esquema
- Cada microservicio accede solo a sus tablas

### Tablas:
- `usuarios` - Usuario Service
- `categorias` - Producto Service
- `productos` - Producto Service
- `producto_tamanos` - Producto Service
- `producto_etiquetas` - Producto Service
- `carrito_items` - Carrito Service
- `ventas` - Ventas Service
- `detalle_ventas` - Ventas Service

## 🚀 Configuración

### Variables de Entorno

Se leen del archivo `.env` (copiar `.env.example`) o de variables de entorno. Nunca se escriben en el código.

| Variable | Usada por | Descripción |
|----------|-----------|-------------|
| `DATABASE_URL` | usuario, producto, carrito, ventas | `jdbc:postgresql://[NEON_HOST]:5432/neondb?sslmode=require` |
| `DATABASE_USERNAME` | usuario, producto, carrito, ventas | Usuario de Neon |
| `DATABASE_PASSWORD` | usuario, producto, carrito, ventas | Contraseña de Neon |
| `JWT_SECRET` | usuario-service, api-gateway | Debe ser el mismo en ambos |
| `*_SERVICE_URL` | api-gateway, bff | URLs de los microservicios (por defecto `localhost`) |
| `FRONTEND_URL` | api-gateway | Origen permitido por CORS |

### Configuración de Neon PostgreSQL

1. Crear cuenta en [Neon](https://neon.tech/)
2. Crear un nuevo proyecto llamado "milsabores"
3. Obtener la cadena de conexión
4. Ejecutar el script `database/schema.sql` en la base de datos

## 📦 Estructura del Repositorio

```
Backend_MilSabores_Rosales_Herrera/
├── api-gateway/            # Spring Cloud Gateway (JWT, rate limiting, CORS)
├── bff/                    # Backend For Frontend (checkout)
├── usuario-service/
│   ├── src/main/java/com/milsabores/usuario/
│   │   ├── config/          # Seguridad, CORS, OpenAPI
│   │   ├── controller/      # REST Controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Excepciones y GlobalExceptionHandler
│   │   ├── model/           # Entidades JPA
│   │   ├── repository/      # Repositorios JPA
│   │   ├── service/         # Lógica de negocio
│   │   └── util/            # JwtUtil
│   ├── src/main/resources/application.properties
│   └── Dockerfile
├── producto-service/       # [misma estructura]
├── carrito-service/        # [misma estructura]
├── ventas-service/         # [misma estructura]
├── k8s/                    # Manifiestos de Kubernetes (kustomize)
├── database/schema.sql     # Script de creación de BD
├── docs/                   # Documentación (arquitectura, endpoints, despliegue)
├── docker-compose.yml      # Entorno local con contenedores
└── render.yaml             # Blueprint de despliegue en Render
```

Cada servicio (incluidos api-gateway y bff) tiene su propio `Dockerfile`, Gradle wrapper y `build.gradle`.

## 🛠️ Construcción y Ejecución

### Prerrequisitos
- JDK 17 o 21. Los proyectos compilan para Java 17. **Gradle 8.11 no funciona con JDK 25**, así que `JAVA_HOME` debe apuntar a JDK 17 o 21.
- Gradle 8.11.1 (incluido en el wrapper `gradlew` de cada servicio)
- Base de datos PostgreSQL (Neon)
- Docker (opcional, para ejecutar con contenedores)

### Opción A: Docker Compose (todo el backend)

```bash
cp .env.example .env        # completar credenciales
docker compose up --build -d
curl http://localhost:8080/actuator/health
```

### Opción B: un microservicio con Gradle

```powershell
cd usuario-service
./gradlew bootRun
```

O generar y ejecutar el JAR:

```powershell
cd usuario-service
./gradlew clean bootJar
java -jar build/libs/usuario-service-0.0.1-SNAPSHOT.jar
```

## 📡 Endpoints Principales

Todos se pueden llamar a través del gateway (`http://localhost:8080`) o directamente en el puerto de cada servicio.

### Usuario Service (http://localhost:8081)

```
POST   /api/usuarios/register         - Registrar usuario (alias: /registro)
POST   /api/usuarios/login            - Iniciar sesión (devuelve JWT)
GET    /api/usuarios                  - Listar usuarios
GET    /api/usuarios/{id}             - Obtener usuario
PUT    /api/usuarios/{id}             - Actualizar usuario
PATCH  /api/usuarios/{id}             - Actualización parcial
DELETE /api/usuarios/{id}             - Eliminar usuario
```

### Producto Service (http://localhost:8082)

```
GET    /api/productos                           - Listar productos
GET    /api/productos/{code}                    - Obtener producto
GET    /api/productos/categoria/{categoriaId}   - Productos por categoría
GET    /api/productos/destacados                - Productos destacados
GET    /api/productos/buscar?nombre={nombre}    - Buscar productos
POST   /api/productos                           - Crear producto
PUT    /api/productos/{code}                    - Actualizar producto
PATCH  /api/productos/{code}/stock?stock={n}    - Fijar stock
PATCH  /api/productos/{code}/reducir-stock      - Descontar stock
DELETE /api/productos/{code}                    - Eliminar producto

GET    /api/categorias                          - Listar categorías
GET    /api/categorias/{id}                     - Obtener categoría
POST   /api/categorias                          - Crear categoría
PUT    /api/categorias/{id}                     - Actualizar categoría
DELETE /api/categorias/{id}                     - Eliminar categoría
```

### Carrito Service (http://localhost:8083)

```
GET    /api/carritos/usuario/{usuarioId}                     - Obtener carrito
POST   /api/carritos/agregar                                 - Agregar item
PUT    /api/carritos/item/{itemId}                           - Actualizar cantidad
DELETE /api/carritos/item/{itemId}                           - Eliminar item
DELETE /api/carritos/usuario/{usuarioId}/producto/{codigo}   - Eliminar producto
DELETE /api/carritos/usuario/{usuarioId}                     - Limpiar carrito
GET    /api/carritos/usuario/{usuarioId}/total               - Obtener total
GET    /api/carritos/usuario/{usuarioId}/cantidad            - Obtener cantidad de items
```

### Ventas Service (http://localhost:8084)

```
POST   /api/ventas                                    - Crear venta
GET    /api/ventas                                    - Listar ventas
GET    /api/ventas/{id}                               - Obtener venta
GET    /api/ventas/usuario/{usuarioId}                - Ventas por usuario
GET    /api/ventas/estado/{estado}                    - Ventas por estado
GET    /api/ventas/fecha?fechaInicio=..&fechaFin=..   - Ventas por rango de fechas (ISO)
PATCH  /api/ventas/{id}/estado                        - Actualizar estado
POST   /api/ventas/{id}/pagar                         - Iniciar pago con Transbank
POST   /api/ventas/{id}/confirmar-pago                - Confirmar pago
POST   /api/ventas/transbank/return?token_ws=..       - Retorno de Webpay
DELETE /api/ventas/{id}                               - Eliminar venta
```

### BFF (http://localhost:8085)

```
GET    /bff/checkout/resumen          - Resumen del carrito con precio y stock verificados
POST   /bff/checkout                  - Crea la venta desde el carrito e inicia el pago
```

## 📚 Documentación API (Swagger)

- Usuario Service: http://localhost:8081/swagger-ui.html
- Producto Service: http://localhost:8082/swagger-ui.html
- Carrito Service: http://localhost:8083/swagger-ui.html
- Ventas Service: http://localhost:8084/swagger-ui.html
- BFF: http://localhost:8085/swagger-ui.html

Colección de Postman: `MilSabores-Postman-Collection.json`.

## ❤️ Health checks

Todos los servicios exponen Spring Boot Actuator:

- `GET /actuator/health`: estado general (lo usan Docker y Render)
- `GET /actuator/health/liveness` y `/actuator/health/readiness`: probes de Kubernetes
- `GET /actuator/circuitbreakers` (BFF): estado de los circuit breakers

## 🚢 Despliegue

La guía completa está en [`docs/DEPLOY.md`](docs/DEPLOY.md):

- **Docker Compose**: `docker-compose.yml` levanta los 6 servicios en local.
- **Kubernetes**: `kubectl apply -k k8s/` (namespace, ConfigMap, Deployments, Services, Ingress).
- **Render**: Blueprint `render.yaml` (6 Web Services Docker).
- **AWS EC2**: ejecutar cada JAR, o su imagen Docker, con las variables de entorno de la tabla anterior.

## 🔧 Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3.4.1**
- **Spring Cloud Gateway** (2024.0.0)
- **Spring Security + JWT** (jjwt 0.12.3)
- **Spring Data JPA** + **PostgreSQL** (Neon)
- **Resilience4j 2.2.0** (circuit breaker y retry)
- **Spring Boot Actuator**
- **SpringDoc OpenAPI 2.7.0** (Swagger)
- **Lombok**
- **Gradle 8.11.1**
- **Docker**, **Kubernetes** y **Render**

## 📝 Notas Importantes

1. **Seguridad**: contraseñas hasheadas con BCrypt en usuario-service.
2. **JWT**: usuario-service firma los tokens y el API Gateway los valida con el mismo `JWT_SECRET`.
3. **Transacciones**: se usa `@Transactional` para mantener la consistencia de los datos.
4. **CORS**: se centraliza en el API Gateway. Agregar nuevos orígenes con `FRONTEND_URL` o en `api-gateway/src/main/resources/application.yml`.
5. **Rate limiting**: se guarda en memoria, así que el gateway debe correr con **una sola réplica**.
6. **Transbank**: se usan las credenciales públicas del ambiente de integración. La URL de retorno apunta al frontend en Vercel.

## ✅ Avance del Proyecto (al 23-09-2026)

### Completado
- [x] Microservicios usuario, producto, carrito y ventas
- [x] Integración con Transbank Webpay Plus (integración)
- [x] Seguridad con Spring Security y JWT
- [x] Contraseñas hasheadas con BCrypt
- [x] API Gateway con validación JWT (Spring Cloud Gateway)
- [x] Rate limiting en el gateway
- [x] BFF para el flujo de checkout
- [x] Circuit Breaker y Retry (Resilience4j en el BFF)
- [x] Health checks con Actuator en todos los servicios
- [x] Documentación Swagger/OpenAPI
- [x] Dockerizar los microservicios
- [x] Manifiestos de Kubernetes
- [x] Blueprint de despliegue en Render

### Pendiente
- [ ] Integrar validación JWT de Azure IDaaS
- [ ] Configurar Azure API Manager (hoy la validación JWT la hace Spring Cloud Gateway)
- [ ] Agregar Redis para caché y para un rate limiting distribuido
- [ ] Métricas con Prometheus (Actuator ya está incorporado)
- [ ] Ampliar tests unitarios e integración
- [ ] Pipeline CI/CD para construir y publicar las imágenes

## 📧 Contacto

Para más información: contacto@milsabores.cl
