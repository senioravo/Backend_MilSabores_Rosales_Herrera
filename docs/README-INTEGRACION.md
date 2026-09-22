# 🚀 Guía de Integración con Neon PostgreSQL

## 📦 Requisitos Previos

- ✅ Java 21 instalado
- ✅ Gradle 8.11.1 o superior
- ✅ Base de datos Neon PostgreSQL creada
- ✅ Schema.sql ejecutado en Neon

## 🔧 Configuración

### 1. Obtener Credenciales de Neon

1. Ve a tu dashboard de [Neon](https://console.neon.tech)
2. Selecciona tu proyecto
3. En la sección **Connection Details**, copia:
   - **Host**: `ep-xxxx-xxxx.us-east-2.aws.neon.tech`
   - **Database**: `neondb`
   - **User**: Tu usuario
   - **Password**: Tu contraseña

### 2. Configurar Variables de Entorno

Edita el archivo `.env` en la raíz del proyecto:

```env
# Ejemplo real de URL de Neon
DATABASE_URL=jdbc:postgresql://ep-cool-union-12345678.us-east-2.aws.neon.tech:5432/neondb?sslmode=require
DATABASE_USERNAME=neondb_owner
DATABASE_PASSWORD=tu_password_aqui

# JWT Secret (cambiar en producción)
JWT_SECRET=milsabores-secret-key-2024-super-segura-para-produccion-cambiar
```

**⚠️ IMPORTANTE**: 
- Agrega `?sslmode=require` al final de la URL
- NO compartas el archivo `.env` (ya está en `.gitignore`)

### 3. Verificar Schema en Neon

Conecta a tu base de datos Neon y verifica que las tablas existan:

```sql
-- Verificar tablas
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Debería mostrar:
-- carrito_items
-- categorias
-- detalle_ventas
-- producto_etiquetas
-- producto_tamanos
-- productos
-- usuarios
-- ventas
```

## 🚀 Ejecutar los Microservicios

### Opción 1: Ejecutar todos los servicios automáticamente

```powershell
.\run-all-services.ps1
```

Este script:
1. Carga las variables de entorno desde `.env`
2. Construye todos los servicios con Gradle
3. Inicia los 4 microservicios en ventanas separadas
4. Muestra las URLs de Swagger UI

### Opción 2: Ejecutar servicios individualmente

#### Usuario Service (Puerto 8081)
```powershell
cd usuario-service
.\gradlew.bat bootRun
```

#### Producto Service (Puerto 8082)
```powershell
cd producto-service
.\gradlew.bat bootRun
```

#### Carrito Service (Puerto 8083)
```powershell
cd carrito-service
.\gradlew.bat bootRun
```

#### Ventas Service (Puerto 8084)
```powershell
cd ventas-service
.\gradlew.bat bootRun
```

## 🧪 Probar la Conexión

### 1. Ejecutar test automático
```powershell
.\test-connection.ps1
```

### 2. Probar manualmente con Swagger UI

Abre en tu navegador:
- Usuario: http://localhost:8081/swagger-ui.html
- Producto: http://localhost:8082/swagger-ui.html
- Carrito: http://localhost:8083/swagger-ui.html
- Ventas: http://localhost:8084/swagger-ui.html

### 3. Probar endpoints básicos

#### Listar Categorías
```powershell
curl http://localhost:8082/api/categorias
```

#### Listar Productos
```powershell
curl http://localhost:8082/api/productos
```

#### Registrar Usuario
```powershell
$body = @{
    nombre = "Test Usuario"
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/usuarios/register" -Method POST -Body $body -ContentType "application/json"
```

## 🔍 Verificación de Funcionamiento

### ✅ Checklist de Verificación

- [ ] Los 4 servicios inician sin errores
- [ ] Swagger UI es accesible en cada puerto
- [ ] GET /api/categorias devuelve 8 categorías
- [ ] GET /api/productos devuelve 16 productos
- [ ] POST /api/usuarios/register crea un usuario y devuelve JWT
- [ ] POST /api/usuarios/login autentica y devuelve JWT
- [ ] GET /api/productos?page=0&size=5 devuelve productos paginados
- [ ] POST /api/carrito crea items en el carrito
- [ ] POST /api/ventas crea una venta

### 📊 Verificar Datos en Neon

Ejecuta en Neon SQL Editor:

```sql
-- Contar registros
SELECT 
    (SELECT COUNT(*) FROM usuarios) as usuarios,
    (SELECT COUNT(*) FROM categorias) as categorias,
    (SELECT COUNT(*) FROM productos) as productos,
    (SELECT COUNT(*) FROM carrito_items) as carrito_items,
    (SELECT COUNT(*) FROM ventas) as ventas,
    (SELECT COUNT(*) FROM detalle_ventas) as detalle_ventas;
```

## 🐛 Solución de Problemas

### Error: Connection refused
- Verifica que la URL de Neon es correcta
- Asegúrate de incluir `?sslmode=require`
- Verifica que el proyecto Neon no está en modo "sleep"

### Error: Authentication failed
- Verifica usuario y contraseña en `.env`
- Revisa que las credenciales son las correctas en Neon Dashboard

### Error: Relation does not exist
- Ejecuta el `schema.sql` completo en Neon SQL Editor
- Verifica que usas la base de datos correcta (`neondb`)

### Error: Port already in use
- Detén los procesos que usan los puertos 8081-8084
- Usa: `netstat -ano | findstr "8081"`

### Error: Gradle build failed
- Limpia el proyecto: `.\gradlew.bat clean`
- Verifica que Java 21 está instalado: `java -version`

## 📝 Logs y Debugging

### Ver logs de SQL
Los servicios están configurados con:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Verás las consultas SQL en la consola de cada servicio.

### Cambiar nivel de logging
Agrega en `application.properties`:
```properties
logging.level.com.example=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

## 🎯 Próximos Pasos

1. ✅ Base de datos configurada
2. ✅ Microservicios funcionando
3. ⏳ Actualizar frontend para usar nuevos endpoints
4. ⏳ Implementar autenticación JWT en frontend
5. ⏳ Probar flujo completo de compra

## 📚 Documentación de APIs

Cada servicio tiene documentación interactiva en Swagger:

| Servicio | Puerto | Swagger UI | Descripción |
|----------|--------|------------|-------------|
| Usuario | 8081 | http://localhost:8081/swagger-ui.html | Registro, login, JWT |
| Producto | 8082 | http://localhost:8082/swagger-ui.html | Catálogo con paginación |
| Carrito | 8083 | http://localhost:8083/swagger-ui.html | Carrito de compras |
| Ventas | 8084 | http://localhost:8084/swagger-ui.html | Ventas y Transbank |

## 🔐 Seguridad

- Las contraseñas se hashean con BCrypt (10 rounds)
- JWT expira en 24 horas (86400000 ms)
- CORS configurado para localhost:5173 (Vite) y localhost:3000 (React)
- Cambiar `JWT_SECRET` en producción

## 📊 Monitoreo

### Health Check
```powershell
# Usuario Service
curl http://localhost:8081/actuator/health

# Producto Service
curl http://localhost:8082/actuator/health
```

### Métricas
```powershell
curl http://localhost:8081/actuator/metrics
```

---

**¿Problemas?** Revisa los logs de cada servicio o ejecuta `.\test-connection.ps1`
