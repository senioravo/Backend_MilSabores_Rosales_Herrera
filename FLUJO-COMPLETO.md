# 🎯 FLUJO COMPLETO - MIL SABORES E-COMMERCE

## 📍 Infraestructura Actual

### AWS EC2
- **IP Elástica:** `100.30.4.167`
- **Instancia:** Ubuntu
- **Base de Datos:** PostgreSQL 17.6 (Neon Cloud)

### Microservicios Operativos ✅

| Servicio | Puerto | URL | Estado |
|----------|--------|-----|--------|
| Usuario | 8081 | http://100.30.4.167:8081/api/usuarios | ✅ Operativo |
| Producto | 8082 | http://100.30.4.167:8082/api/productos | ✅ Operativo |
| Carrito | 8083 | http://100.30.4.167:8083/api/carritos | ✅ Operativo |
| Ventas | 8084 | http://100.30.4.167:8084/api/ventas | ✅ Operativo |

---

## 🔄 FLUJO COMPLETO DE USUARIO

### 1️⃣ **REGISTRO DE USUARIO**

**Endpoint:** `POST http://100.30.4.167:8081/api/usuarios/register`

**Request:**
```json
{
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com",
  "password": "password123",
  "telefono": "+56912345678",
  "direccion": "Av. Principal 123, Santiago"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com",
  "telefono": "+56912345678",
  "direccion": "Av. Principal 123, Santiago",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFuLnBlcmV6QGV4YW1wbGUuY29tIiwiaWQiOjEsIm5vbWJyZSI6Ikp1YW4gUMOpcmV6IiwiaWF0IjoxNzMzMDk2NDAwLCJleHAiOjE3MzMxODI4MDB9.xyz...",
  "message": "Usuario registrado exitosamente"
}
```

**Características:**
- ✅ Password hasheado con BCrypt
- ✅ JWT Token generado automáticamente
- ✅ Expiración: 24 horas
- ✅ Validación de email único

---

### 2️⃣ **LOGIN**

**Endpoint:** `POST http://100.30.4.167:8081/api/usuarios/login`

**Request:**
```json
{
  "email": "juan.perez@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com",
  "telefono": "+56912345678",
  "direccion": "Av. Principal 123, Santiago",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "Login exitoso"
}
```

**Headers para requests autenticados:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

### 3️⃣ **ACTUALIZAR PERFIL (Parcial)**

**Endpoint:** `PATCH http://100.30.4.167:8081/api/usuarios/1`

**Request (solo enviar campos a modificar):**
```json
{
  "telefono": "+56987654321",
  "direccion": "Nueva Dirección 456"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com",
  "telefono": "+56987654321",
  "direccion": "Nueva Dirección 456"
}
```

---

## 🛍️ FLUJO DE COMPRA

### 4️⃣ **LISTAR PRODUCTOS CON PAGINACIÓN**

**Endpoint:** `GET http://100.30.4.167:8082/api/productos?page=0&size=10`

**Response (200 OK):**
```json
{
  "content": [
    {
      "code": "PROD-001",
      "nombre": "Torta de Chocolate",
      "descripcion": "Deliciosa torta de chocolate con ganache",
      "precio": 15000,
      "stock": 25,
      "imagen": "/images/products/torta-chocolate.jpg",
      "categoriaId": "tortas",
      "personalizable": true,
      "destacado": true,
      "activo": true
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 45,
  "totalPages": 5,
  "last": false
}
```

---

### 5️⃣ **FILTRAR PRODUCTOS**

**Endpoint:** `GET http://100.30.4.167:8082/api/productos?categoriaId=tortas&minPrecio=10000&maxPrecio=20000&sortBy=precio&sortDir=asc`

**Parámetros disponibles:**
- `page` - Número de página (default: 0)
- `size` - Tamaño de página (default: 10)
- `categoriaId` - Filtrar por categoría
- `minPrecio` - Precio mínimo
- `maxPrecio` - Precio máximo
- `personalizable` - true/false
- `sortBy` - Campo para ordenar (nombre, precio)
- `sortDir` - Dirección (asc, desc)

---

### 6️⃣ **AGREGAR AL CARRITO**

**Endpoint:** `POST http://100.30.4.167:8083/api/carritos/agregar`

**Request:**
```json
{
  "usuarioId": 1,
  "productoCode": "PROD-001",
  "cantidad": 2
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "productoCode": "PROD-001",
  "productoNombre": "Torta de Chocolate",
  "productoImagen": "/images/products/torta-chocolate.jpg",
  "cantidad": 2,
  "precioUnitario": 15000,
  "subtotal": 30000
}
```

---

### 7️⃣ **VER CARRITO COMPLETO**

**Endpoint:** `GET http://100.30.4.167:8083/api/carritos/usuario/1`

**Response (200 OK):**
```json
{
  "id": 1,
  "usuarioId": 1,
  "items": [
    {
      "id": 1,
      "productoCode": "PROD-001",
      "productoNombre": "Torta de Chocolate",
      "productoImagen": "/images/products/torta-chocolate.jpg",
      "cantidad": 2,
      "precioUnitario": 15000,
      "subtotal": 30000
    },
    {
      "id": 2,
      "productoCode": "PROD-005",
      "productoNombre": "Pastel de Fresa",
      "productoImagen": "/images/products/pastel-fresa.jpg",
      "cantidad": 1,
      "precioUnitario": 12000,
      "subtotal": 12000
    }
  ],
  "cantidadTotal": 3,
  "totalCarrito": 42000
}
```

---

### 8️⃣ **CREAR VENTA (Checkout)**

**Endpoint:** `POST http://100.30.4.167:8084/api/ventas`

**Request:**
```json
{
  "usuarioId": 1,
  "usuarioNombre": "Juan Pérez",
  "usuarioEmail": "juan.perez@example.com",
  "detalles": [
    {
      "productoCode": "PROD-001",
      "productoNombre": "Torta de Chocolate",
      "productoImagen": "/images/products/torta-chocolate.jpg",
      "cantidad": 2,
      "precioUnitario": 15000,
      "subtotal": 30000
    },
    {
      "productoCode": "PROD-005",
      "productoNombre": "Pastel de Fresa",
      "productoImagen": "/images/products/pastel-fresa.jpg",
      "cantidad": 1,
      "precioUnitario": 12000,
      "subtotal": 12000
    }
  ],
  "subtotal": 42000,
  "iva": 7980,
  "total": 49980
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "usuarioId": 1,
  "usuarioNombre": "Juan Pérez",
  "usuarioEmail": "juan.perez@example.com",
  "detalles": [...],
  "subtotal": 42000,
  "iva": 7980,
  "total": 49980,
  "estado": "PENDIENTE",
  "fechaCreacion": "2025-12-02T20:30:00",
  "transbankToken": null,
  "transbankOrderId": null
}
```

**Efectos automáticos:**
- ✅ Stock de productos reducido automáticamente
- ✅ Venta creada en estado PENDIENTE

---

### 9️⃣ **PROCESAR PAGO CON TRANSBANK**

**Endpoint:** `POST http://100.30.4.167:8084/api/ventas/1/pagar`

**Response (200 OK):**
```json
{
  "token": "TBK-a1b2c3d4e5f6",
  "url": "https://webpay3gint.transbank.cl/webpayserver/initTransaction",
  "ordenCompra": "ORD-20251202-001",
  "monto": 49980,
  "exitoso": true,
  "mensaje": "Simulación de pago Transbank iniciado correctamente"
}
```

**Simulación de Transbank:**
- 80% de probabilidad de éxito
- 20% de probabilidad de rechazo
- Token y orden de compra generados

---

### 🔟 **CONFIRMAR PAGO**

**Endpoint:** `POST http://100.30.4.167:8084/api/ventas/1/confirmar-pago?token=TBK-a1b2c3d4e5f6&exitoso=true`

**Response (200 OK):**
```json
{
  "id": 1,
  "usuarioId": 1,
  "usuarioNombre": "Juan Pérez",
  "usuarioEmail": "juan.perez@example.com",
  "detalles": [...],
  "subtotal": 42000,
  "iva": 7980,
  "total": 49980,
  "estado": "PAGADA",
  "fechaCreacion": "2025-12-02T20:30:00",
  "fechaPago": "2025-12-02T20:32:15",
  "transbankToken": "TBK-a1b2c3d4e5f6",
  "transbankOrderId": "ORD-20251202-001"
}
```

**Estados posibles:**
- `PENDIENTE` - Venta creada, esperando pago
- `PAGADA` - Pago confirmado exitosamente
- `RECHAZADA` - Pago rechazado por Transbank
- `CANCELADA` - Venta cancelada por usuario/sistema

---

## 📊 ENDPOINTS ADICIONALES

### Obtener ventas por usuario
```
GET http://100.30.4.167:8084/api/ventas/usuario/1
```

### Obtener venta específica
```
GET http://100.30.4.167:8084/api/ventas/1
```

### Limpiar carrito después de compra
```
DELETE http://100.30.4.167:8083/api/carritos/usuario/1
```

### Modificar cantidad en carrito
```
PUT http://100.30.4.167:8083/api/carritos/item/1?cantidad=3
```

### Eliminar item del carrito
```
DELETE http://100.30.4.167:8083/api/carritos/item/1
```

---

## 🔐 SEGURIDAD

### JWT Token
- **Algoritmo:** HS256
- **Secret:** Configurable vía variable de entorno `JWT_SECRET`
- **Expiración:** 24 horas (86400000 ms)
- **Incluye:** id, email, nombre del usuario

### Password Hashing
- **Algoritmo:** BCrypt
- **Rounds:** 10 (default)

### CORS
- **Orígenes permitidos:** `*` (cualquier origen para desarrollo)
- **Métodos:** GET, POST, PUT, PATCH, DELETE, OPTIONS
- **Headers:** Todos permitidos
- **Configuración:** Global en application.properties (sin @CrossOrigin en controllers)

---

## ✅ REQUERIMIENTOS CUMPLIDOS

### ✅ Sistema de Registro y Login (users)
- ✅ POST crear usuario con HASH password (BCrypt)
- ✅ POST login con Bearer Token JWT
- ✅ PATCH modificar perfil parcialmente

### ✅ Sistema de Productos (products)
- ✅ GET productos con paginación (Spring Data Page)
- ✅ GET productos con filtros (categoría, precio, personalizable)
- ✅ PATCH reducir stock al vender

### ✅ Sistema de Ventas (sales)
- ✅ POST crear venta con usuario + productos + IVA
- ✅ POST procesar pago Transbank (simulado)
- ✅ POST confirmar pago (éxito/rechazo)
- ✅ Reducción automática de stock

---

## 🚀 TESTING RÁPIDO

### Curl Examples

**1. Registrar usuario:**
```bash
curl -X POST http://100.30.4.167:8081/api/usuarios/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test User",
    "email": "test@example.com",
    "password": "test123",
    "telefono": "+56912345678",
    "direccion": "Test Address 123"
  }'
```

**2. Login:**
```bash
curl -X POST http://100.30.4.167:8081/api/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123"
  }'
```

**3. Listar productos paginados:**
```bash
curl http://100.30.4.167:8082/api/productos?page=0&size=5
```

**4. Crear venta:**
```bash
curl -X POST http://100.30.4.167:8084/api/ventas \
  -H "Content-Type: application/json" \
  -d '{
    "usuarioId": 1,
    "usuarioNombre": "Test User",
    "usuarioEmail": "test@example.com",
    "detalles": [
      {
        "productoCode": "PROD-001",
        "productoNombre": "Torta de Chocolate",
        "cantidad": 1,
        "precioUnitario": 15000,
        "subtotal": 15000
      }
    ],
    "subtotal": 15000,
    "iva": 2850,
    "total": 17850
  }'
```

---

## 📝 NOTAS

- **Frontend:** Ya configurado para usar IP elástica `100.30.4.167`
- **Backend:** CORS actualizado para permitir conexiones desde cualquier origen
- **Base de Datos:** PostgreSQL en Neon Cloud con pooling HikariCP
- **Logs:** Disponibles en `~/logs/` en EC2
- **Scripts:** `start-all.sh` y `stop-all.sh` para gestión de servicios

---

**Última actualización:** Diciembre 2, 2025  
**Estado:** ✅ Todos los microservicios operativos
