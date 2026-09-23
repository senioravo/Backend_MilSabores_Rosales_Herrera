# Backend Mil Sabores - Guía de Inicio Rápido

## 🚀 Inicio Rápido

### 1. Configurar Base de Datos Neon

1. Ve a [Neon Console](https://console.neon.tech/)
2. Crea un nuevo proyecto llamado "milsabores"
3. Copia la cadena de conexión
4. Ejecuta el script `database/schema.sql` en tu base de datos Neon

### 2. Configurar Variables de Entorno

Crea un archivo `.env` en cada carpeta de microservicio (usuario-service, producto-service, carrito-service):

```properties
DATABASE_URL=jdbc:postgresql://[TU_HOST_NEON]/milsabores?sslmode=require
DATABASE_USERNAME=[TU_USUARIO]
DATABASE_PASSWORD=[TU_PASSWORD]
```

O edita directamente el archivo `src/main/resources/application.properties` de cada microservicio.

### 3. Ejecutar los Microservicios

#### Opción A: Usando Gradle (Desarrollo)

**Terminal 1 - Usuario Service:**
```powershell
cd usuario-service
./gradlew bootRun
```

**Terminal 2 - Producto Service:**
```powershell
cd producto-service
./gradlew bootRun
```

**Terminal 3 - Carrito Service:**
```powershell
cd carrito-service
./gradlew bootRun
```

#### Opción B: Usando JAR (Producción)

```powershell
# Compilar todos los servicios
cd usuario-service; ./gradlew build; cd ..
cd producto-service; ./gradlew build; cd ..
cd carrito-service; ./gradlew build; cd ..

# Ejecutar
java -jar usuario-service/build/libs/usuario-service-0.0.1-SNAPSHOT.jar
java -jar producto-service/build/libs/producto-service-0.0.1-SNAPSHOT.jar
java -jar carrito-service/build/libs/carrito-service-0.0.1-SNAPSHOT.jar
```

### 4. Verificar que los servicios están funcionando

- Usuario Service: http://localhost:8081/swagger-ui.html
- Producto Service: http://localhost:8082/swagger-ui.html
- Carrito Service: http://localhost:8083/swagger-ui.html

### 5. Configurar Frontend

Actualiza las URLs de los servicios en tu frontend React (archivo de configuración o servicios).

## 🧪 Probar los Endpoints

### Registrar Usuario
```bash
curl -X POST http://localhost:8081/api/usuarios/registro \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez",
    "email": "juan@example.com",
    "password": "password123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8081/api/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@example.com",
    "password": "password123"
  }'
```

### Listar Productos
```bash
curl http://localhost:8082/api/productos
```

### Agregar al Carrito
```bash
curl -X POST http://localhost:8083/api/carrito/agregar \
  -H "Content-Type: application/json" \
  -d '{
    "usuarioId": 1,
    "productoCode": "TC001",
    "productoNombre": "Torta Cuadrada de Chocolate",
    "precioCLP": 45000,
    "productoImagen": "TC001.png",
    "cantidad": 2,
    "stockDisponible": 10
  }'
```

## 🐛 Troubleshooting

### Error de conexión a base de datos
- Verifica que las credenciales de Neon sean correctas
- Asegúrate de que el script `schema.sql` se haya ejecutado
- Verifica que la URL incluya `?sslmode=require` para Neon

### Puerto ya en uso
```powershell
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

### Gradle build falla
```powershell
./gradlew clean build --refresh-dependencies
```

## 📦 Estructura de Respuestas

### Usuario Response
```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "fechaRegistro": "2024-01-15T10:30:00",
  "activo": true
}
```

### Producto Response
```json
{
  "code": "TC001",
  "nombre": "Torta Cuadrada de Chocolate",
  "categoria": {
    "id": "TC",
    "nombre": "Tortas Cuadradas",
    "descripcion": "Tortas de forma cuadrada en varios tamaños.",
    "imagen": "PG.png"
  },
  "tipoForma": "cuadrada",
  "tamanosDisponibles": ["S (8 porciones)", "M (12 porciones)", "L (20 porciones)"],
  "precioCLP": 45000,
  "stock": 10,
  "personalizable": true,
  "maxMsgChars": 50,
  "descripcion": "Deliciosa torta de chocolate...",
  "etiquetas": ["tradicional"],
  "imagen": "TC001.png",
  "activo": true
}
```

### Carrito Response
```json
{
  "usuarioId": 1,
  "items": [
    {
      "id": 1,
      "usuarioId": 1,
      "productoCode": "TC001",
      "productoNombre": "Torta Cuadrada de Chocolate",
      "precioCLP": 45000,
      "productoImagen": "TC001.png",
      "cantidad": 2,
      "stockDisponible": 10,
      "fechaAgregado": "2024-01-15T10:30:00",
      "subtotal": 90000
    }
  ],
  "totalItems": 2,
  "totalPrecio": 90000
}
```
