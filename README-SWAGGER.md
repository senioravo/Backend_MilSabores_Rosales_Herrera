# 🎉 Swagger UI - Mil Sabores Backend

## ✅ Estado del Sistema

Todos los microservicios están funcionando correctamente con Swagger UI operativo.

## 🚀 Iniciar Servicios

```powershell
cd C:\Users\Alex\Documents\BackendMilSabores
.\run-all-services.ps1
```

El script:
1. Carga variables de entorno desde `.env`
2. Compila todos los servicios con Gradle
3. Inicia cada servicio en una ventana separada de PowerShell
4. Cada servicio se conecta a Neon PostgreSQL automáticamente

## 📡 URLs de Acceso

### Swagger UI (Interfaz Interactiva)
- **Usuario Service**: http://localhost:8081/swagger-ui.html
- **Producto Service**: http://localhost:8082/swagger-ui.html
- **Carrito Service**: http://localhost:8083/swagger-ui.html
- **Ventas Service**: http://localhost:8084/swagger-ui.html

### API Docs (JSON OpenAPI)
- **Usuario Service**: http://localhost:8081/v3/api-docs
- **Producto Service**: http://localhost:8082/v3/api-docs
- **Carrito Service**: http://localhost:8083/v3/api-docs
- **Ventas Service**: http://localhost:8084/v3/api-docs

## 🔧 Configuración Técnica

### Versiones
- **Spring Boot**: 3.4.1
- **SpringDoc OpenAPI**: 2.7.0 (actualizado para compatibilidad con Spring Boot 3.4.x)
- **Java**: 17.0.17
- **Gradle**: 9.1.0
- **PostgreSQL**: Neon Cloud (v17.6)

### Dependencias Clave
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'
```

### Configuración application.properties
```properties
# OpenAPI/Swagger Configuration
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.default-produces-media-type=application/json
springdoc.default-consumes-media-type=application/json
springdoc.packages-to-scan=com.milsabores.<service>.controller
springdoc.model-converters.pageable-converter.enabled=true
```

## 🛠️ Solución de Problemas

### Error 500 en /v3/api-docs
**Causa**: Incompatibilidad entre SpringDoc 2.6.0 y Spring Boot 3.4.1
**Solución**: Actualizar a SpringDoc 2.7.0 ✅

### Servicio no se conecta a la base de datos
**Causa**: Variables de entorno no cargadas
**Solución**: Ejecutar con `run-all-services.ps1` que carga `.env` automáticamente

### Puerto ya en uso
```powershell
# Detener todos los servicios
@(8081, 8082, 8083, 8084) | ForEach-Object { 
    $p = Get-NetTCPConnection -LocalPort $_ -ErrorAction SilentlyContinue | 
         Select-Object -ExpandProperty OwningProcess -First 1
    if ($p) { Stop-Process -Id $p -Force }
}
```

## 📝 Características de Swagger UI

### Usuario Service (Puerto 8081)
- ✅ Registro de usuarios
- ✅ Login con JWT
- ✅ Autenticación Bearer Token
- 🔐 Endpoints protegidos con JWT

### Producto Service (Puerto 8082)
- ✅ Listar productos con paginación
- ✅ Filtros por categoría, precio, personalizable
- ✅ Búsqueda por nombre
- ✅ CRUD completo de productos y categorías
- 📊 Consultas optimizadas con JPA Criteria API

### Carrito Service (Puerto 8083)
- ✅ Gestión de carritos de compra
- ✅ Agregar/eliminar items
- ✅ Calcular totales
- 🛒 Sincronización con stock

### Ventas Service (Puerto 8084)
- ✅ Crear ventas con detalles
- ✅ Integración simulada con Transbank
- ✅ Estados de pago (PENDIENTE, APROBADO, RECHAZADO)
- ✅ Historial de compras por usuario
- 💳 80% de probabilidad de aprobación en pagos

## 🎯 Pruebas con Swagger UI

### 1. Registrar Usuario
```
POST /api/usuarios/register
Body:
{
  "nombre": "Test User",
  "email": "test@example.com",
  "password": "Password123",
  "telefono": "+56912345678",
  "direccion": "Calle Falsa 123",
  "rol": "CLIENTE"
}
```
Respuesta incluye JWT token automáticamente.

### 2. Listar Productos
```
GET /api/productos?page=0&size=10&categoriaId=TC&minPrecio=10000
```

### 3. Crear Venta
```
POST /api/ventas
Headers: Authorization: Bearer <JWT_TOKEN>
Body:
{
  "usuarioId": 1,
  "metodoPago": "TRANSBANK",
  "direccionEnvio": "Calle Falsa 123",
  "detalles": [
    {
      "productoCode": "TC001",
      "cantidad": 2,
      "precioUnitario": 25000
    }
  ]
}
```

## 📚 Documentación Adicional

- **OpenAPI Spec**: Cada servicio expone su especificación completa en `/v3/api-docs`
- **Swagger UI**: Interfaz interactiva para probar todos los endpoints
- **Validaciones**: Todas las request/response están documentadas con sus validaciones
- **Ejemplos**: Cada endpoint incluye ejemplos de uso

## ✨ Funcionalidades Destacadas

1. **Autenticación JWT**: Sistema completo de autenticación con Bearer tokens
2. **Paginación**: Soporte para paginación en consultas de productos
3. **Filtros Dinámicos**: Múltiples criterios de búsqueda y filtrado
4. **Validaciones**: Bean Validation (JSR-380) en todos los DTOs
5. **CORS**: Configurado para desarrollo con frontend local
6. **Documentación Completa**: Todos los endpoints documentados con Swagger
7. **Manejo de Errores**: Respuestas consistentes con códigos HTTP apropiados

## 🔐 Seguridad

- **JWT**: Tokens con expiración de 24 horas
- **BCrypt**: Hash seguro de contraseñas
- **CORS**: Configurado para orígenes permitidos
- **Validaciones**: Validación de entrada en todos los endpoints

## 🌐 Base de Datos

- **Proveedor**: Neon PostgreSQL (Serverless)
- **Conexión**: SSL requerido
- **Pooling**: HikariCP
- **ORM**: Hibernate + JPA

---

**Nota**: Para detener los servicios, presiona `Ctrl+C` en cada ventana de PowerShell donde están corriendo.
