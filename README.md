# Backend Mil Sabores - Arquitectura de Microservicios

Sistema backend desarrollado con Spring Boot para la aplicación Mil Sabores, implementado como microservicios independientes.

## 🏗️ Arquitectura

El backend está compuesto por 3 microservicios independientes:

### 1. Usuario Service (Puerto 8081)
- Gestión de usuarios y autenticación
- Registro y login de usuarios
- CRUD completo de usuarios

### 2. Producto Service (Puerto 8082)
- Gestión de productos y categorías
- CRUD de productos
- CRUD de categorías
- Búsquedas y filtros
- Productos destacados

### 3. Carrito Service (Puerto 8083)
- Gestión del carrito de compras
- Agregar/eliminar productos
- Actualizar cantidades
- Cálculo de totales

## 🗄️ Base de Datos

**PostgreSQL en Neon**
- Base de datos: `milsabores`
- Todas las tablas están en el mismo esquema
- Cada microservicio accede a sus tablas correspondientes

### Tablas:
- `usuarios` - Usuario Service
- `categorias` - Producto Service
- `productos` - Producto Service
- `producto_tamanos` - Producto Service
- `producto_etiquetas` - Producto Service
- `carrito_items` - Carrito Service

## 🚀 Configuración

### Variables de Entorno

Cada microservicio requiere las siguientes variables de entorno (o se pueden configurar en `application.properties`):

```properties
DATABASE_URL=jdbc:postgresql://[NEON_HOST]/milsabores
DATABASE_USERNAME=[NEON_USERNAME]
DATABASE_PASSWORD=[NEON_PASSWORD]
```

### Configuración de Neon PostgreSQL

1. Crear cuenta en [Neon](https://neon.tech/)
2. Crear un nuevo proyecto llamado "milsabores"
3. Obtener la cadena de conexión
4. Ejecutar el script `database/schema.sql` en la base de datos

## 📦 Estructura de Proyectos

```
BackendMilSabores/
├── usuario-service/
│   ├── src/main/java/com/milsabores/usuario/
│   │   ├── config/          # Configuraciones (CORS, OpenAPI)
│   │   ├── controller/      # REST Controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Excepciones personalizadas
│   │   ├── model/           # Entidades JPA
│   │   ├── repository/      # Repositorios JPA
│   │   ├── service/         # Lógica de negocio
│   │   └── UsuarioServiceApplication.java
│   └── src/main/resources/
│       └── application.properties
│
├── producto-service/
│   └── [misma estructura]
│
├── carrito-service/
│   └── [misma estructura]
│
└── database/
    └── schema.sql          # Script de creación de BD
```

## 🛠️ Construcción y Ejecución

### Prerrequisitos
- Java 21
- Gradle 8.11.1 o superior
- PostgreSQL (Neon)

### Compilar un microservicio

```powershell
cd usuario-service
./gradlew build
```

### Ejecutar un microservicio

```powershell
cd usuario-service
./gradlew bootRun
```

O ejecutar el JAR generado:

```powershell
java -jar build/libs/usuario-service-0.0.1-SNAPSHOT.jar
```

### Generar JAR para producción

```powershell
cd usuario-service
./gradlew clean build
```

El JAR se generará en: `build/libs/usuario-service-0.0.1-SNAPSHOT.jar`

## 📡 Endpoints Principales

### Usuario Service (http://localhost:8081)

```
POST   /api/usuarios/registro         - Registrar usuario
POST   /api/usuarios/login            - Iniciar sesión
GET    /api/usuarios                  - Listar usuarios
GET    /api/usuarios/{id}             - Obtener usuario
PUT    /api/usuarios/{id}             - Actualizar usuario
DELETE /api/usuarios/{id}             - Eliminar usuario
```

### Producto Service (http://localhost:8082)

```
GET    /api/productos                           - Listar todos los productos
GET    /api/productos/{code}                    - Obtener producto
GET    /api/productos/categoria/{categoriaId}   - Productos por categoría
GET    /api/productos/destacados                - Productos destacados
GET    /api/productos/buscar?nombre={nombre}    - Buscar productos
POST   /api/productos                           - Crear producto
PUT    /api/productos/{code}                    - Actualizar producto
PATCH  /api/productos/{code}/stock              - Actualizar stock
DELETE /api/productos/{code}                    - Eliminar producto

GET    /api/categorias                          - Listar categorías
GET    /api/categorias/{id}                     - Obtener categoría
POST   /api/categorias                          - Crear categoría
PUT    /api/categorias/{id}                     - Actualizar categoría
DELETE /api/categorias/{id}                     - Eliminar categoría
```

### Carrito Service (http://localhost:8083)

```
GET    /api/carrito/usuario/{usuarioId}                       - Obtener carrito
POST   /api/carrito/agregar                                   - Agregar item
PUT    /api/carrito/item/{itemId}?cantidad={cantidad}         - Actualizar cantidad
DELETE /api/carrito/item/{itemId}                             - Eliminar item
DELETE /api/carrito/usuario/{usuarioId}/producto/{codigo}     - Eliminar producto
DELETE /api/carrito/usuario/{usuarioId}                       - Limpiar carrito
GET    /api/carrito/usuario/{usuarioId}/total                 - Obtener total
GET    /api/carrito/usuario/{usuarioId}/cantidad              - Obtener cantidad items
```

## 📚 Documentación API (Swagger)

Cada microservicio expone su documentación en:

- Usuario Service: http://localhost:8081/swagger-ui.html
- Producto Service: http://localhost:8082/swagger-ui.html
- Carrito Service: http://localhost:8083/swagger-ui.html

## 🔒 CORS

Los microservicios están configurados para aceptar peticiones desde:
- http://localhost:5173 (Vite dev server)
- http://localhost:3000 (React dev server)

## 🚢 Despliegue en AWS EC2

### Pasos para desplegar cada microservicio:

1. **Generar JAR**:
   ```powershell
   ./gradlew clean build
   ```

2. **Subir JAR a EC2**:
   ```powershell
   scp -i tu-clave.pem build/libs/usuario-service-0.0.1-SNAPSHOT.jar ec2-user@tu-instancia:/home/ec2-user/
   ```

3. **Configurar variables de entorno en EC2**:
   ```bash
   export DATABASE_URL="jdbc:postgresql://[NEON_HOST]/milsabores"
   export DATABASE_USERNAME="[NEON_USERNAME]"
   export DATABASE_PASSWORD="[NEON_PASSWORD]"
   ```

4. **Ejecutar el microservicio**:
   ```bash
   java -jar usuario-service-0.0.1-SNAPSHOT.jar
   ```

5. **Usar systemd para gestionar el servicio** (Opcional):
   Crear archivo `/etc/systemd/system/usuario-service.service`:
   ```ini
   [Unit]
   Description=Usuario Service
   After=syslog.target

   [Service]
   User=ec2-user
   ExecStart=/usr/bin/java -jar /home/ec2-user/usuario-service-0.0.1-SNAPSHOT.jar
   SuccessExitStatus=143
   Environment="DATABASE_URL=jdbc:postgresql://[NEON_HOST]/milsabores"
   Environment="DATABASE_USERNAME=[NEON_USERNAME]"
   Environment="DATABASE_PASSWORD=[NEON_PASSWORD]"

   [Install]
   WantedBy=multi-user.target
   ```

   Habilitar y arrancar:
   ```bash
   sudo systemctl enable usuario-service
   sudo systemctl start usuario-service
   ```

## 🔧 Tecnologías Utilizadas

- **Java 21**
- **Spring Boot 3.4.1**
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **SpringDoc OpenAPI 3 (Swagger)**
- **Gradle 8.11.1**

## 📝 Notas Importantes

1. **Seguridad**: Las contraseñas actualmente NO están hasheadas. Para producción, implementar BCrypt.
2. **JWT**: Los tokens JWT no están implementados aún. La autenticación es básica.
3. **Transacciones**: Se usa `@Transactional` para garantizar consistencia de datos.
4. **CORS**: Configurado para desarrollo local. Ajustar para producción.
5. **Logging**: Spring Boot proporciona logging por defecto. Revisar logs con `tail -f logs/spring.log`.

## 🔄 Próximas Mejoras

- [ ] Implementar seguridad con Spring Security y JWT
- [ ] Hashear contraseñas con BCrypt
- [ ] Agregar Redis para caché
- [ ] Implementar API Gateway
- [ ] Agregar Circuit Breaker (Resilience4j)
- [ ] Implementar service discovery (Eureka)
- [ ] Agregar métricas y monitoring (Actuator + Prometheus)
- [ ] Tests unitarios e integración
- [ ] Dockerizar los microservicios

## 📧 Contacto

Para más información: contacto@milsabores.cl
