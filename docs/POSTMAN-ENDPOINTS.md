# 📮 ENDPOINTS PARA POSTMAN - MIL SABORES

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
**Body (JSON):**
```json
{
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com",
  "password": "password123",
  "telefono": "+56912345678",
  "direccion": "Av. Principal 123, Santiago"
}
```

### 2. Login
```
POST http://100.30.4.167:8081/api/usuarios/login
```
**Headers:**
```
Content-Type: application/json
```
**Body (JSON):**
```json
{
  "email": "juan.perez@example.com",
  "password": "password123"
}
```
**Response:** Guardar el `token` para requests autenticados

### 3. Obtener Todos los Usuarios
```
GET http://100.30.4.167:8081/api/usuarios
```

### 4. Obtener Usuario por ID
```
GET http://100.30.4.167:8081/api/usuarios/1
```

### 5. Actualizar Usuario Completo
```
PUT http://100.30.4.167:8081/api/usuarios/1
```
**Headers:**
```
Content-Type: application/json
```
**Body (JSON):**
```json
{
  "nombre": "Juan Pérez Actualizado",
  "email": "juan.perez@example.com",
  "password": "newpassword123",
  "telefono": "+56987654321",
  "direccion": "Nueva Dirección 456"
}
```

### 6. Actualizar Usuario Parcial (PATCH)
```
PATCH http://100.30.4.167:8081/api/usuarios/1
```
**Headers:**
```
Content-Type: application/json
```
**Body (JSON):** Solo incluir campos a modificar
```json
{
  "telefono": "+56911111111",
  "direccion": "Dirección Modificada 789"
}
```

### 7. Eliminar Usuario
```
DELETE http://100.30.4.167:8081/api/usuarios/1
```

---

## 🛍️ PRODUCTO SERVICE (Puerto 8082)

### 8. Obtener Todos los Productos (Simple)
```
GET http://100.30.4.167:8082/api/productos
```

### 9. Obtener Productos con Paginación
```
GET http://100.30.4.167:8082/api/productos?page=0&size=10
```

### 10. Filtrar Productos por Categoría
```
GET http://100.30.4.167:8082/api/productos?categoriaId=tortas
```

### 11. Filtrar Productos por Rango de Precio
```
GET http://100.30.4.167:8082/api/productos?minPrecio=5000&maxPrecio=20000
```

### 12. Filtrar Productos Personalizables
```
GET http://100.30.4.167:8082/api/productos?personalizable=true
```

### 13. Filtros Combinados con Paginación y Ordenamiento
```
GET http://100.30.4.167:8082/api/productos?page=0&size=5&categoriaId=tortas&minPrecio=10000&maxPrecio=30000&sortBy=precio&sortDir=asc
```

### 14. Obtener Producto por Código
```
GET http://100.30.4.167:8082/api/productos/PROD-001
```

### 15. Obtener Productos por Categoría (Legacy)
```
GET http://100.30.4.167:8082/api/productos/categoria/tortas
```

### 16. Obtener Productos Destacados
```
GET http://100.30.4.167:8082/api/productos/destacados
```

### 17. Obtener Productos Destacados con Límite
```
GET http://100.30.4.167:8082/api/productos/destacados?limit=5
```

### 18. Buscar Productos por Nombre
```
GET http://100.30.4.167:8082/api/productos/buscar?nombre=chocolate
```

### 19. Crear Producto
```
POST http://100.30.4.167:8082/api/productos
```
**Headers:**
```
Content-Type: application/json
```
**Body (JSON):**
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

### 20. Actualizar Producto Completo
```
PUT http://100.30.4.167:8082/api/productos/PROD-999
```
**Headers:**
```
Content-Type: application/json
```
**Body (JSON):**
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

### 21. Actualizar Stock de Producto
```
PATCH http://100.30.4.167:8082/api/productos/PROD-001/stock?stock=50
```

### 22. Reducir Stock al Vender
```
PATCH http://100.30.4.167:8082/api/productos/PROD-001/reducir-stock?cantidad=2
```

### 23. Eliminar Producto
```
DELETE http://100.30.4.167:8082/api/productos/PROD-999
```

---

## 🛒 CARRITO SERVICE (Puerto 8083)

### 24. Obtener Carrito de Usuario
```
GET http://100.30.4.167:8083/api/carritos/usuario/1
```

### 25. Agregar Item al Carrito
```
POST http://100.30.4.167:8083/api/carritos/agregar
```
**Headers:**
```
Content-Type: application/json
```
**Body (JSON):**
```json
{
  "usuarioId": 1,
  "productoCode": "PROD-001",
  "cantidad": 2
}
```

### 26. Actualizar Cantidad de Item
```
PUT http://100.30.4.167:8083/api/carritos/item/1?cantidad=5
```

### 27. Eliminar Item del Carrito por ID
```
DELETE http://100.30.4.167:8083/api/carritos/item/1
```

### 28. Eliminar Item por Usuario y Producto
```
DELETE http://100.30.4.167:8083/api/carritos/usuario/1/producto/PROD-001
```

### 29. Limpiar Carrito Completo
```
DELETE http://100.30.4.167:8083/api/carritos/usuario/1
```

### 30. Obtener Total del Carrito
```
GET http://100.30.4.167:8083/api/carritos/usuario/1/total
```

### 31. Obtener Cantidad de Items
```
GET http://100.30.4.167:8083/api/carritos/usuario/1/cantidad
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
**Body (JSON):**
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

### 33. Obtener Venta por ID
```
GET http://100.30.4.167:8084/api/ventas/1
```

### 34. Obtener Todas las Ventas
```
GET http://100.30.4.167:8084/api/ventas
```

### 35. Obtener Ventas por Usuario
```
GET http://100.30.4.167:8084/api/ventas/usuario/1
```

### 36. Obtener Ventas por Estado
```
GET http://100.30.4.167:8084/api/ventas/estado/PENDIENTE
```
**Estados disponibles:** PENDIENTE, PAGADA, RECHAZADA, CANCELADA

### 37. Actualizar Estado de Venta
```
PATCH http://100.30.4.167:8084/api/ventas/1/estado?estado=PAGADA
```

### 38. Procesar Pago con Transbank
```
POST http://100.30.4.167:8084/api/ventas/1/pagar
```
**Response:** Guardar el `token` para confirmar pago

### 39. Confirmar Pago Exitoso
```
POST http://100.30.4.167:8084/api/ventas/1/confirmar-pago?token=TBK-a1b2c3d4e5f6&exitoso=true
```

### 40. Confirmar Pago Rechazado
```
POST http://100.30.4.167:8084/api/ventas/1/confirmar-pago?token=TBK-a1b2c3d4e5f6&exitoso=false
```

### 41. Obtener Ventas por Rango de Fechas
```
GET http://100.30.4.167:8084/api/ventas/fecha?fechaInicio=2025-12-01T00:00:00&fechaFin=2025-12-31T23:59:59
```

### 42. Eliminar Venta
```
DELETE http://100.30.4.167:8084/api/ventas/1
```

---

## 🔄 FLUJO COMPLETO DE COMPRA (Secuencia)

### Paso 1: Registrar Usuario
```
POST http://100.30.4.167:8081/api/usuarios/register
```

### Paso 2: Login
```
POST http://100.30.4.167:8081/api/usuarios/login
```
→ Guardar `id` y `token`

### Paso 3: Ver Productos
```
GET http://100.30.4.167:8082/api/productos?page=0&size=10
```

### Paso 4: Agregar al Carrito (Producto 1)
```
POST http://100.30.4.167:8083/api/carritos/agregar
Body: {"usuarioId": 1, "productoCode": "PROD-001", "cantidad": 2}
```

### Paso 5: Agregar al Carrito (Producto 2)
```
POST http://100.30.4.167:8083/api/carritos/agregar
Body: {"usuarioId": 1, "productoCode": "PROD-005", "cantidad": 1}
```

### Paso 6: Ver Carrito
```
GET http://100.30.4.167:8083/api/carritos/usuario/1
```
→ Anotar items y calcular totales

### Paso 7: Crear Venta
```
POST http://100.30.4.167:8084/api/ventas
Body: {usuarioId, usuarioNombre, usuarioEmail, detalles[], subtotal, iva, total}
```
→ Guardar `id` de venta

### Paso 8: Procesar Pago
```
POST http://100.30.4.167:8084/api/ventas/1/pagar
```
→ Guardar `token` de Transbank

### Paso 9: Confirmar Pago
```
POST http://100.30.4.167:8084/api/ventas/1/confirmar-pago?token=TBK-xxx&exitoso=true
```

### Paso 10: Limpiar Carrito
```
DELETE http://100.30.4.167:8083/api/carritos/usuario/1
```

### Paso 11: Ver Mis Ventas
```
GET http://100.30.4.167:8084/api/ventas/usuario/1
```

---

## 📊 COLECCIÓN POSTMAN

### Variables de Entorno Sugeridas
```
base_url = http://100.30.4.167
usuario_port = 8081
producto_port = 8082
carrito_port = 8083
ventas_port = 8084

# Después del login
user_id = 1
auth_token = eyJhbGciOiJIUzI1NiJ9...
```

### Uso en Postman
```
{{base_url}}:{{usuario_port}}/api/usuarios/login
```

---

## 🧪 TESTS RÁPIDOS

### Health Check de Todos los Servicios
```bash
# Usuario
GET http://100.30.4.167:8081/api/usuarios

# Producto
GET http://100.30.4.167:8082/api/productos

# Carrito
GET http://100.30.4.167:8083/api/carritos/usuario/1

# Ventas
GET http://100.30.4.167:8084/api/ventas
```

### Swagger UI
```
http://100.30.4.167:8081/swagger-ui.html  # Usuario
http://100.30.4.167:8082/swagger-ui.html  # Producto
http://100.30.4.167:8083/swagger-ui.html  # Carrito
http://100.30.4.167:8084/swagger-ui.html  # Ventas
```

---

## 📝 NOTAS

- Todos los endpoints soportan CORS desde cualquier origen
- Los passwords se hashean automáticamente con BCrypt
- JWT tokens expiran en 24 horas
- La simulación de Transbank tiene 80% de probabilidad de éxito
- El stock se reduce automáticamente al crear una venta
- IVA = 19% del subtotal

**Última actualización:** Diciembre 2, 2025
