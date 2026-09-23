# 📮 ENDPOINTS COMPLETOS CON BODIES - MIL SABORES

**Base URL:** `http://100.30.4.167`

---

## 👤 USUARIO SERVICE (Puerto 8081)

### 1. Registrar Usuario
```
POST http://100.30.4.167:8081/api/usuarios/register
```
**Headers:**
```
Content-Type: application/json
```
**Body:**
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
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "Usuario registrado exitosamente"
}
```

---

### 2. Login
```
POST http://100.30.4.167:8081/api/usuarios/login
```
**Headers:**
```
Content-Type: application/json
```
**Body:**
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

---

### 3. Obtener Todos los Usuarios
```
GET http://100.30.4.167:8081/api/usuarios
```
**Headers:** Ninguno requerido

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "nombre": "Juan Pérez",
    "email": "juan.perez@example.com",
    "telefono": "+56912345678",
    "direccion": "Av. Principal 123, Santiago"
  }
]
```

---

### 4. Obtener Usuario por ID
```
GET http://100.30.4.167:8081/api/usuarios/1
```
**Headers:** Ninguno requerido

**Response (200 OK):**
```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com",
  "telefono": "+56912345678",
  "direccion": "Av. Principal 123, Santiago"
}
```

---

### 5. Actualizar Usuario Completo
```
PUT http://100.30.4.167:8081/api/usuarios/1
```
**Headers:**
```
Content-Type: application/json
```
**Body:**
```json
{
  "nombre": "Juan Pérez Actualizado",
  "email": "juan.perez@example.com",
  "password": "newpassword123",
  "telefono": "+56987654321",
  "direccion": "Nueva Dirección 456"
}
```
**Response (200 OK):**
```json
{
  "id": 1,
  "nombre": "Juan Pérez Actualizado",
  "email": "juan.perez@example.com",
  "telefono": "+56987654321",
  "direccion": "Nueva Dirección 456"
}
```

---

### 6. Actualizar Usuario Parcial (PATCH)
```
PATCH http://100.30.4.167:8081/api/usuarios/1
```
**Headers:**
```
Content-Type: application/json
```
**Body (Solo campos a modificar):**
```json
{
  "telefono": "+56911111111",
  "direccion": "Dirección Modificada 789"
}
```
**Response (200 OK):**
```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com",
  "telefono": "+56911111111",
  "direccion": "Dirección Modificada 789"
}
```

---

### 7. Eliminar Usuario
```
DELETE http://100.30.4.167:8081/api/usuarios/1
```
**Headers:** Ninguno requerido

**Response (204 No Content):** Sin body

---

## 🛍️ PRODUCTO SERVICE (Puerto 8082)

### 8. Obtener Todos los Productos (Simple)
```
GET http://100.30.4.167:8082/api/productos
```
**Headers:** Ninguno requerido

**Response (200 OK):**
```json
[
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
]
```

---

### 9. Obtener Productos con Paginación
```
GET http://100.30.4.167:8082/api/productos?page=0&size=10
```
**Headers:** Ninguno requerido

**Response (200 OK):**
```json
{
  "content": [
    {
      "code": "PROD-001",
      "nombre": "Torta de Chocolate",
      "descripcion": "Deliciosa torta de chocolate",
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

### 10. Filtrar Productos por Categoría
```
GET http://100.30.4.167:8082/api/productos?categoriaId=tortas
```

---

### 11. Filtrar Productos por Rango de Precio
```
GET http://100.30.4.167:8082/api/productos?minPrecio=5000&maxPrecio=20000
```

---

### 12. Filtrar Productos Personalizables
```
GET http://100.30.4.167:8082/api/productos?personalizable=true
```

---

### 13. Filtros Combinados con Paginación
```
GET http://100.30.4.167:8082/api/productos?page=0&size=5&categoriaId=tortas&minPrecio=10000&maxPrecio=30000&sortBy=precio&sortDir=asc
```

---

### 14. Obtener Producto por Código
```
GET http://100.30.4.167:8082/api/productos/PROD-001
```
**Response (200 OK):**
```json
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
```

---

### 15. Obtener Productos por Categoría
```
GET http://100.30.4.167:8082/api/productos/categoria/tortas
```

---

### 16. Obtener Productos Destacados
```
GET http://100.30.4.167:8082/api/productos/destacados
```

---

### 17. Obtener Productos Destacados con Límite
```
GET http://100.30.4.167:8082/api/productos/destacados?limit=5
```

---

### 18. Buscar Productos por Nombre
```
GET http://100.30.4.167:8082/api/productos/buscar?nombre=chocolate
```

---

### 19. Crear Producto
```
POST http://100.30.4.167:8082/api/productos
```
**Headers:**
```
Content-Type: application/json
```
**Body:**
```json
{
  "code": "PROD-999",
  "nombre": "Torta Test",
  "descripcion": "Torta de prueba",
  "precio": 18000,
  "stock": 10,
  "imagen": "/images/products/torta-test.jpg",
  "categoriaId": "tortas",
  "personalizable": true,
  "destacado": false
}
```
**Response (201 Created):**
```json
{
  "code": "PROD-999",
  "nombre": "Torta Test",
  "descripcion": "Torta de prueba",
  "precio": 18000,
  "stock": 10,
  "imagen": "/images/products/torta-test.jpg",
  "categoriaId": "tortas",
  "personalizable": true,
  "destacado": false,
  "activo": true
}
```

---

### 20. Actualizar Producto Completo
```
PUT http://100.30.4.167:8082/api/productos/PROD-999
```
**Headers:**
```
Content-Type: application/json
```
**Body:**
```json
{
  "nombre": "Torta Test Actualizada",
  "descripcion": "Torta de prueba actualizada",
  "precio": 20000,
  "stock": 15,
  "imagen": "/images/products/torta-test-v2.jpg",
  "categoriaId": "tortas",
  "personalizable": true,
  "destacado": true
}
```

---

### 21. Actualizar Stock de Producto
```
PATCH http://100.30.4.167:8082/api/productos/PROD-001/stock?stock=50
```
**Headers:** Ninguno requerido

**Response (200 OK):**
```json
{
  "code": "PROD-001",
  "nombre": "Torta de Chocolate",
  "stock": 50,
  "...": "..."
}
```

---

### 22. Reducir Stock al Vender
```
PATCH http://100.30.4.167:8082/api/productos/PROD-001/reducir-stock?cantidad=2
```
**Headers:** Ninguno requerido

**Response (200 OK):**
```json
{
  "code": "PROD-001",
  "nombre": "Torta de Chocolate",
  "stock": 23,
  "...": "..."
}
```

---

### 23. Eliminar Producto
```
DELETE http://100.30.4.167:8082/api/productos/PROD-999
```
**Response (204 No Content):** Sin body

---

## 🛒 CARRITO SERVICE (Puerto 8083)

### 24. Obtener Carrito de Usuario
```
GET http://100.30.4.167:8083/api/carritos/usuario/1
```
**Headers:** Ninguno requerido

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
    }
  ],
  "cantidadTotal": 2,
  "totalCarrito": 30000
}
```

---

### 25. Agregar Item al Carrito
```
POST http://100.30.4.167:8083/api/carritos/agregar
```
**Headers:**
```
Content-Type: application/json
```
**Body:**
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

### 26. Actualizar Cantidad de Item
```
PUT http://100.30.4.167:8083/api/carritos/item/1?cantidad=5
```
**Headers:** Ninguno requerido

**Response (200 OK):**
```json
{
  "id": 1,
  "productoCode": "PROD-001",
  "productoNombre": "Torta de Chocolate",
  "productoImagen": "/images/products/torta-chocolate.jpg",
  "cantidad": 5,
  "precioUnitario": 15000,
  "subtotal": 75000
}
```

---

### 27. Eliminar Item del Carrito por ID
```
DELETE http://100.30.4.167:8083/api/carritos/item/1
```
**Response (204 No Content):** Sin body

---

### 28. Eliminar Item por Usuario y Producto
```
DELETE http://100.30.4.167:8083/api/carritos/usuario/1/producto/PROD-001
```
**Response (204 No Content):** Sin body

---

### 29. Limpiar Carrito Completo
```
DELETE http://100.30.4.167:8083/api/carritos/usuario/1
```
**Response (204 No Content):** Sin body

---

### 30. Obtener Total del Carrito
```
GET http://100.30.4.167:8083/api/carritos/usuario/1/total
```
**Response (200 OK):**
```json
30000
```

---

### 31. Obtener Cantidad de Items
```
GET http://100.30.4.167:8083/api/carritos/usuario/1/cantidad
```
**Response (200 OK):**
```json
2
```

---

## 💰 VENTAS SERVICE (Puerto 8084)

### 32. Crear Venta
```
POST http://100.30.4.167:8084/api/ventas
```
**Headers:**
```
Content-Type: application/json
```
**Body:**
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
  "detalles": [
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
  "subtotal": 42000,
  "iva": 7980,
  "total": 49980,
  "estado": "PENDIENTE",
  "fechaCreacion": "2025-12-02T20:30:00",
  "transbankToken": null,
  "transbankOrderId": null
}
```

---

### 33. Obtener Venta por ID
```
GET http://100.30.4.167:8084/api/ventas/1
```
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
  "estado": "PENDIENTE",
  "fechaCreacion": "2025-12-02T20:30:00"
}
```

---

### 34. Obtener Todas las Ventas
```
GET http://100.30.4.167:8084/api/ventas
```
**Response (200 OK):**
```json
[
  {
    "id": 1,
    "usuarioId": 1,
    "total": 49980,
    "estado": "PENDIENTE",
    "fechaCreacion": "2025-12-02T20:30:00"
  }
]
```

---

### 35. Obtener Ventas por Usuario
```
GET http://100.30.4.167:8084/api/ventas/usuario/1
```

---

### 36. Obtener Ventas por Estado
```
GET http://100.30.4.167:8084/api/ventas/estado/PENDIENTE
```
**Estados disponibles:** PENDIENTE, PAGADA, RECHAZADA, CANCELADA

---

### 37. Actualizar Estado de Venta
```
PATCH http://100.30.4.167:8084/api/ventas/1/estado?estado=PAGADA
```
**Response (200 OK):**
```json
{
  "id": 1,
  "estado": "PAGADA",
  "...": "..."
}
```

---

### 38. Procesar Pago con Transbank
```
POST http://100.30.4.167:8084/api/ventas/1/pagar
```
**Headers:** Ninguno requerido

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

---

### 39. Confirmar Pago Exitoso
```
POST http://100.30.4.167:8084/api/ventas/1/confirmar-pago?token=TBK-a1b2c3d4e5f6&exitoso=true
```
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

---

### 40. Confirmar Pago Rechazado
```
POST http://100.30.4.167:8084/api/ventas/1/confirmar-pago?token=TBK-a1b2c3d4e5f6&exitoso=false
```
**Response (200 OK):**
```json
{
  "id": 1,
  "estado": "RECHAZADA",
  "...": "..."
}
```

---

### 41. Obtener Ventas por Rango de Fechas
```
GET http://100.30.4.167:8084/api/ventas/fecha?fechaInicio=2025-12-01T00:00:00&fechaFin=2025-12-31T23:59:59
```

---

### 42. Eliminar Venta
```
DELETE http://100.30.4.167:8084/api/ventas/1
```
**Response (204 No Content):** Sin body

---

## 🔄 FLUJO COMPLETO DE COMPRA

### Paso 1: Registrar Usuario
```
POST http://100.30.4.167:8081/api/usuarios/register
Body: {"nombre": "...", "email": "...", "password": "..."}
```

### Paso 2: Login
```
POST http://100.30.4.167:8081/api/usuarios/login
Body: {"email": "...", "password": "..."}
→ Guardar token
```

### Paso 3: Ver Productos
```
GET http://100.30.4.167:8082/api/productos?page=0&size=10
```

### Paso 4: Agregar al Carrito
```
POST http://100.30.4.167:8083/api/carritos/agregar
Body: {"usuarioId": 1, "productoCode": "PROD-001", "cantidad": 2}
```

### Paso 5: Ver Carrito
```
GET http://100.30.4.167:8083/api/carritos/usuario/1
```

### Paso 6: Crear Venta
```
POST http://100.30.4.167:8084/api/ventas
Body: {usuarioId, usuarioNombre, usuarioEmail, detalles[], subtotal, iva, total}
```

### Paso 7: Procesar Pago
```
POST http://100.30.4.167:8084/api/ventas/1/pagar
→ Guardar token de Transbank
```

### Paso 8: Confirmar Pago
```
POST http://100.30.4.167:8084/api/ventas/1/confirmar-pago?token=TBK-xxx&exitoso=true
```

### Paso 9: Limpiar Carrito
```
DELETE http://100.30.4.167:8083/api/carritos/usuario/1
```

### Paso 10: Ver Historial
```
GET http://100.30.4.167:8084/api/ventas/usuario/1
```

---

## 📝 NOTAS

- **IVA:** 19% del subtotal (Chile)
- **Password:** Enviar sin hashear, BCrypt lo procesa automáticamente
- **JWT Token:** Expira en 24 horas
- **Transbank:** Simulación con 80% probabilidad de éxito
- **Stock:** Se reduce automáticamente al crear venta

---

**Última actualización:** Diciembre 2, 2025  
**IP Elástica EC2:** 100.30.4.167  
**Estado:** ✅ Todos los servicios operativos
