# RESPUESTAS EVALUACIÓN TÉCNICA - MIL SABORES

**Proyecto:** Sistema E-commerce Mil Sabores  
**Arquitectura:** Microservicios Spring Boot 3.4.1 + React 18  
**Base de Datos:** PostgreSQL 17.6 (Neon Cloud)  
**Autor:** Alex Rosales Herrera  
**Fecha:** Diciembre 2025

---

## RESPUESTAS 1-10

### 1. ¿Qué modelo de base de datos implementaron y cómo se relaciona con los requisitos del cliente?

Implementamos un modelo relacional en PostgreSQL con 4 dominios principales:

- **Usuarios** (usuario-service): Entidad `Usuario` con campos id, nombre, email, password (BCrypt), fechaRegistro, activo. Soporta autenticación JWT para gestión de sesiones.

- **Productos** (producto-service): Entidad `Producto` con id, nombre, descripción, precio, stock, categoriaId, imagen, activo. Relación ManyToOne con `Categoria` para organización del catálogo de pastelería.

- **Carrito** (carrito-service): Entidades `Carrito` (usuarioId, fechaCreacion) y `CarritoItem` (carritoId, productoId, cantidad, precioUnitario). Relación OneToMany entre Carrito-Items para gestionar compras temporales.

- **Ventas** (ventas-service): Entidades `Venta` (usuarioId, usuarioNombre, usuarioEmail, fechaVenta, subtotal, iva, total, estado) y `DetalleVenta` (ventaId, productoCode, productoNombre, cantidad, precioUnitario, subtotal). Relación OneToMany para persistir órdenes completadas con integración Transbank.

El modelo separa datos transaccionales (ventas) de operacionales (carrito) cumpliendo requisitos de trazabilidad y escalabilidad por dominio.

---

### 2. ¿Cómo configuraron la conexión a la base de datos en Spring Boot y qué propiedades fueron necesarias?

```properties
# application.properties (todos los servicios)
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/milsabores}
spring.datasource.username=${DATABASE_USERNAME:postgres}
spring.datasource.password=${DATABASE_PASSWORD:password}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

Variables de entorno en `.env`:
```
DATABASE_URL=jdbc:postgresql://ep-noisy-glade-acnt8zv8-pooler.sa-east-1.aws.neon.tech:5432/neondb?sslmode=require
DATABASE_USERNAME=neondb_owner
DATABASE_PASSWORD=npg_5CjH6VAeioaF
JWT_SECRET=milsabores-secret-key-2024-super-segura-para-produccion-cambiar
```

HikariCP maneja el connection pool automáticamente. SSL requerido por Neon con `sslmode=require`. DDL-auto en `update` para sincronización automática de esquema sin pérdida de datos.

---

### 3. ¿Qué entidades crearon y cómo decidieron sus relaciones (OneToMany, ManyToMany, etc.)?

**Usuario Service:**
```java
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @Column(unique = true) private String email;
    private String password; // BCrypt
    private LocalDateTime fechaRegistro;
    private Boolean activo;
}
```

**Producto Service:**
```java
@Entity
@Table(name = "productos")
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
    private Long categoriaId; // FK soft
    private String imagen;
}
```

**Carrito Service:**
```java
@Entity
@Table(name = "carritos")
public class Carrito {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long usuarioId; // FK soft
    
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarritoItem> items;
}

@Entity
@Table(name = "carrito_items")
public class CarritoItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id")
    private Carrito carrito;
    
    private Long productoId; // FK soft
    private Integer cantidad;
    private Double precioUnitario;
}
```

**Ventas Service:**
```java
@Entity
@Table(name = "ventas")
public class Venta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long usuarioId; // FK soft
    private LocalDateTime fechaVenta;
    private Integer subtotal;
    private Integer iva;
    private Integer total;
    
    @Enumerated(EnumType.STRING)
    private EstadoVenta estado; // PENDIENTE, PAGADA, CANCELADA
    
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles;
}

@Entity
@Table(name = "detalle_ventas")
public class DetalleVenta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta;
    
    private String productoCode;
    private String productoNombre;
    private Integer cantidad;
    private Integer precioUnitario;
    private Integer subtotal;
}
```

Decidimos **Foreign Keys soft** (Long ids) entre microservicios para mantener independencia y evitar transacciones distribuidas. **OneToMany** con `cascade.ALL` y `orphanRemoval=true` para gestión automática de items dependientes. **FetchType.LAZY** para optimizar queries y evitar N+1.

---

### 4. ¿Cómo aseguraron que la lógica de negocio cumple los requerimientos del cliente?

**Service Layer con reglas de negocio:**

```java
@Service
@Transactional
public class VentaService {
    public VentaResponseDTO crearVenta(VentaRequestDTO request) {
        // Validación: mínimo 1 producto
        if (request.getDetalles().isEmpty()) {
            throw new BusinessException("Venta debe tener al menos un producto");
        }
        
        // Validación: totales coherentes
        int subtotalCalculado = request.getDetalles().stream()
            .mapToInt(d -> d.getCantidad() * d.getPrecioUnitario())
            .sum();
        
        if (subtotalCalculado != request.getSubtotal()) {
            throw new BusinessException("Subtotal inconsistente");
        }
        
        // Lógica: IVA 19%
        int ivaCalculado = (int) Math.round(subtotalCalculado * 0.19);
        if (Math.abs(ivaCalculado - request.getIva()) > 1) {
            throw new BusinessException("IVA calculado incorrectamente");
        }
        
        // Persistencia con estado inicial
        Venta venta = new Venta();
        venta.setEstado(EstadoVenta.PENDIENTE);
        venta.setFechaVenta(LocalDateTime.now());
        // ... mapeo DTO → Entity
        
        return mapper.toDTO(ventaRepository.save(venta));
    }
}
```

```java
@Service
public class CarritoService {
    @Transactional
    public CarritoItemDTO agregarItem(Long usuarioId, Long productoId, int cantidad) {
        // Validación: producto existe y tiene stock
        Producto producto = productoClient.getById(productoId);
        if (producto.getStock() < cantidad) {
            throw new BusinessException("Stock insuficiente");
        }
        
        // Lógica: obtener o crear carrito
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
            .orElseGet(() -> crearNuevoCarrito(usuarioId));
        
        // Lógica: actualizar cantidad si ya existe
        Optional<CarritoItem> existente = carrito.getItems().stream()
            .filter(i -> i.getProductoId().equals(productoId))
            .findFirst();
        
        if (existente.isPresent()) {
            existente.get().setCantidad(existente.get().getCantidad() + cantidad);
        } else {
            CarritoItem item = new CarritoItem();
            item.setCarrito(carrito);
            item.setProductoId(productoId);
            item.setCantidad(cantidad);
            item.setPrecioUnitario(producto.getPrecio());
            carrito.getItems().add(item);
        }
        
        return mapper.toDTO(carritoRepository.save(carrito));
    }
}
```

Tests unitarios con JUnit 5 y Mockito validan escenarios edge cases. GlobalExceptionHandler centraliza respuestas de error consistentes.

---

### 5. ¿Qué validaciones se implementaron a nivel de servicio o entidad?

**Bean Validation (JSR-380) en DTOs:**

```java
@Data
public class VentaRequestDTO {
    @NotNull(message = "Usuario ID requerido")
    private Long usuarioId;
    
    @NotBlank(message = "Nombre usuario requerido")
    private String usuarioNombre;
    
    @Email(message = "Email inválido")
    @NotBlank(message = "Email requerido")
    private String usuarioEmail;
    
    @NotNull(message = "Detalles requeridos")
    @Size(min = 1, message = "Mínimo 1 producto")
    private List<DetalleVentaDTO> detalles;
    
    @NotNull @Positive(message = "Subtotal debe ser positivo")
    private Integer subtotal;
    
    @NotNull @PositiveOrZero
    private Integer iva;
    
    @NotNull @Positive
    private Integer total;
}

@Data
public class UsuarioRegisterDTO {
    @NotBlank @Size(min = 3, max = 100)
    private String nombre;
    
    @Email @NotBlank
    private String email;
    
    @NotBlank @Size(min = 6, message = "Mínimo 6 caracteres")
    private String password;
}
```

**Validación en Controllers:**
```java
@PostMapping
public ResponseEntity<VentaResponseDTO> crear(
    @Valid @RequestBody VentaRequestDTO request,
    BindingResult result
) {
    if (result.hasErrors()) {
        throw new ValidationException(result.getAllErrors());
    }
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ventaService.crearVenta(request));
}
```

**Validación en Service Layer:**
```java
@Service
public class ProductoService {
    public Producto actualizar(Long id, ProductoDTO dto) {
        Producto producto = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        
        // Validación negocio: stock no negativo
        if (dto.getStock() != null && dto.getStock() < 0) {
            throw new BusinessException("Stock no puede ser negativo");
        }
        
        // Validación negocio: precio positivo
        if (dto.getPrecio() != null && dto.getPrecio() <= 0) {
            throw new BusinessException("Precio debe ser mayor a 0");
        }
        
        mapper.updateEntity(dto, producto);
        return repository.save(producto);
    }
}
```

**GlobalExceptionHandler:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage
            ));
        
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("Validación fallida", errors));
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

---

### 6. ¿Qué capas componen su arquitectura Backend y qué responsabilidad tiene cada una?

**Arquitectura en capas por microservicio:**

```
Controller Layer (REST API)
├── UsuarioController.java
├── ProductoController.java
├── CarritoController.java
└── VentaController.java
    ↓ Recibe requests HTTP
    ↓ Valida DTOs (@Valid)
    ↓ Delega a Service
    ↓ Mapea respuestas HTTP

Service Layer (Lógica de Negocio)
├── UsuarioService.java
├── ProductoService.java
├── CarritoService.java
└── VentaService.java
    ↓ Reglas de negocio
    ↓ Validaciones complejas
    ↓ Coordinación transaccional (@Transactional)
    ↓ Mapeo Entity ↔ DTO

Repository Layer (Persistencia)
├── UsuarioRepository.java (extends JpaRepository)
├── ProductoRepository.java
├── CarritoRepository.java
└── VentaRepository.java
    ↓ Abstracción CRUD
    ↓ Queries personalizadas
    ↓ Spring Data JPA

Entity Layer (Modelo de Dominio)
├── Usuario.java (@Entity)
├── Producto.java
├── Carrito.java / CarritoItem.java
└── Venta.java / DetalleVenta.java
    ↓ Mapeo JPA
    ↓ Relaciones (@OneToMany, @ManyToOne)

DTO Layer (Transferencia de Datos)
├── UsuarioDTO.java / LoginDTO.java
├── ProductoDTO.java
├── CarritoDTO.java
└── VentaRequestDTO.java / VentaResponseDTO.java
    ↓ Contratos API
    ↓ Desacoplamiento Entity-API

Config Layer (Configuración)
├── CorsConfig.java
├── SecurityConfig.java
├── JwtConfig.java
└── SwaggerConfig.java
    ↓ CORS origins
    ↓ PasswordEncoder (BCrypt)
    ↓ JWT secret/expiration
    ↓ OpenAPI docs

Exception Layer (Manejo de Errores)
├── GlobalExceptionHandler.java (@RestControllerAdvice)
├── BusinessException.java
├── EntityNotFoundException.java
└── ValidationException.java
    ↓ Centraliza errores HTTP
    ↓ Respuestas consistentes
```

**Responsabilidades específicas:**

- **Controller:** Endpoints REST, validación sintáctica, status codes HTTP, serialización JSON.
- **Service:** Lógica de negocio, validación semántica, transacciones, coordinación entre repositorios.
- **Repository:** Abstracción base de datos, queries SQL automáticas, operaciones CRUD.
- **Entity:** Modelo de dominio persistente, relaciones JPA, constraints DB.
- **DTO:** Contratos API externos, versionado, separación Entity-API.

---

### 7. ¿Por qué eligieron JPA/Hibernate y qué ventajas les otorgó?

**Ventajas implementadas:**

1. **Abstracción SQL:** Spring Data JPA genera queries automáticamente sin escribir SQL manual.
```java
// Sin JPA (JDBC puro)
String sql = "SELECT * FROM productos WHERE categoria_id = ? AND activo = true";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setLong(1, categoriaId);
ResultSet rs = stmt.executeQuery();
// ... mapeo manual de ResultSet a objetos

// Con JPA
List<Producto> productos = productoRepository
    .findByCategoriaIdAndActivoTrue(categoriaId);
```

2. **CRUD sin código:** Métodos `save()`, `findById()`, `findAll()`, `delete()` automáticos heredando `JpaRepository`.

3. **Type Safety:** Queries compiladas, detección de errores en tiempo de compilación vs SQL strings.

4. **Lazy Loading optimizado:** Evita N+1 problems con `@EntityGraph` y `fetch joins`.
```java
@Query("SELECT c FROM Carrito c JOIN FETCH c.items WHERE c.usuarioId = :usuarioId")
Optional<Carrito> findByUsuarioIdWithItems(@Param("usuarioId") Long usuarioId);
```

5. **Gestión automática de transacciones:** `@Transactional` maneja commits/rollbacks sin código boilerplate.

6. **Migraciones manejables:** `ddl-auto=update` sincroniza cambios de entidades a schema DB sin pérdida de datos.

7. **Paginación built-in:**
```java
Page<Producto> findByCategoria(Long categoriaId, Pageable pageable);
// GET /productos?page=0&size=20&sort=nombre,asc
```

8. **Cambio de DB transparente:** Migramos de localhost PostgreSQL a Neon cloud solo cambiando `DATABASE_URL` sin modificar código.

9. **Auditoría automática:** `@CreatedDate`, `@LastModifiedDate` con Spring Data JPA Auditing.

10. **Cache de segundo nivel:** HikariCP pool + Hibernate cache reducen latencia DB.

---

### 8. ¿Cómo manejaron errores en el backend? ¿Usaron @ControllerAdvice?

**GlobalExceptionHandler centralizado:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // Validaciones Bean Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                error -> error.getDefaultMessage(),
                (existing, replacement) -> existing
            ));
        
        ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Error de validación")
            .errors(fieldErrors)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // Entidades no encontradas
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    // Excepciones de negocio
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // Errores inesperados
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Error no controlado: ", ex);
        
        ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("Error interno del servidor")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    // Constraint violations SQL
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = "Violación de integridad de datos";
        
        if (ex.getMessage().contains("duplicate key")) {
            message = "El registro ya existe";
        } else if (ex.getMessage().contains("foreign key")) {
            message = "Referencia inválida";
        }
        
        ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value())
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
```

**ErrorResponse DTO estandarizado:**
```java
@Data
@Builder
public class ErrorResponse {
    private int status;
    private String message;
    private Map<String, String> errors;
    private LocalDateTime timestamp;
}
```

**Excepciones custom:**
```java
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entity, Long id) {
        super(String.format("%s con ID %d no encontrado", entity, id));
    }
}
```

**Uso en Services:**
```java
@Service
public class ProductoService {
    public Producto getById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Producto", id));
    }
    
    public Producto reducirStock(Long id, int cantidad) {
        Producto producto = getById(id);
        
        if (producto.getStock() < cantidad) {
            throw new BusinessException(
                String.format("Stock insuficiente. Disponible: %d, Solicitado: %d", 
                    producto.getStock(), cantidad)
            );
        }
        
        producto.setStock(producto.getStock() - cantidad);
        return repository.save(producto);
    }
}
```

---

### 9. ¿Cómo implementaron la paginación o filtrado en algún endpoint?

**ProductoRepository con queries derivadas:**
```java
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    // Paginación básica (heredada de JpaRepository)
    Page<Producto> findAll(Pageable pageable);
    
    // Filtrado por categoría con paginación
    Page<Producto> findByCategoriaId(Long categoriaId, Pageable pageable);
    
    // Búsqueda por nombre con paginación
    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    
    // Filtros combinados
    @Query("SELECT p FROM Producto p WHERE " +
           "(:categoriaId IS NULL OR p.categoriaId = :categoriaId) AND " +
           "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "p.activo = true")
    Page<Producto> findByFilters(
        @Param("categoriaId") Long categoriaId,
        @Param("nombre") String nombre,
        Pageable pageable
    );
}
```

**ProductoController con parámetros paginación:**
```java
@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    
    @GetMapping
    public ResponseEntity<Page<ProductoDTO>> getAll(
        @RequestParam(required = false) Long categoriaId,
        @RequestParam(required = false) String nombre,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "nombre") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Producto> productos;
        
        if (categoriaId != null || nombre != null) {
            productos = productoRepository.findByFilters(categoriaId, nombre, pageable);
        } else {
            productos = productoRepository.findAll(pageable);
        }
        
        Page<ProductoDTO> dtos = productos.map(productoMapper::toDTO);
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<Page<ProductoDTO>> getByCategoria(
        @PathVariable Long categoriaId,
        @PageableDefault(size = 20, sort = "nombre") Pageable pageable
    ) {
        Page<ProductoDTO> productos = productoRepository
            .findByCategoriaId(categoriaId, pageable)
            .map(productoMapper::toDTO);
        
        return ResponseEntity.ok(productos);
    }
}
```

**Respuesta JSON paginada:**
```json
GET /api/productos?page=0&size=10&sortBy=precio&sortDirection=DESC&categoriaId=2

{
  "content": [
    {"id": 15, "nombre": "Torta Premium", "precio": 35000},
    {"id": 8, "nombre": "Torta Especial", "precio": 25000}
  ],
  "pageable": {
    "sort": {"sorted": true, "unsorted": false},
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0
  },
  "totalPages": 3,
  "totalElements": 28,
  "last": false,
  "first": true,
  "size": 10,
  "number": 0,
  "numberOfElements": 10
}
```

**Consumo desde React:**
```javascript
// src/services/productService.js
export const fetchProductosPaginados = async (params) => {
    const { page = 0, size = 20, categoriaId, nombre } = params;
    
    const queryParams = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        ...(categoriaId && { categoriaId: categoriaId.toString() }),
        ...(nombre && { nombre })
    });
    
    const response = await fetch(
        `${API_CONFIG.PRODUCTO_SERVICE}/productos?${queryParams}`
    );
    
    return response.json(); // Page object con content[], totalPages, etc.
};
```

**VentaRepository con fechas:**
```java
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    
    Page<Venta> findByUsuarioId(Long usuarioId, Pageable pageable);
    
    @Query("SELECT v FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    Page<Venta> findByFechaVentaBetween(
        @Param("inicio") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin,
        Pageable pageable
    );
    
    Page<Venta> findByEstado(EstadoVenta estado, Pageable pageable);
}
```

---

### 10. ¿Qué técnicas usaron para asegurar que su backend es escalable o mantenible?

**1. Arquitectura de Microservicios:**
- 4 servicios independientes (usuario, producto, carrito, ventas) desplegables por separado
- Escalado horizontal independiente por dominio (podemos escalar solo producto-service si hay alta carga)
- Puertos segregados (8081-8084) facilitan balanceo de carga futuro

**2. Stateless Services:**
- Autenticación JWT sin sesiones en servidor
- Estado del carrito persiste en DB, no en memoria servidor
- Permite múltiples instancias sin sticky sessions

**3. Separación de Capas:**
```
Controller → Service → Repository → Entity
```
- Cambios en lógica de negocio no afectan Controllers
- Cambios en DB no afectan Services
- Testeable unitariamente por capa

**4. DTOs para Versionado:**
```java
// v1: VentaRequestDTO
// v2: VentaRequestDTOv2 (nuevos campos)
// Entity Venta no cambia
```
- Desacopla API pública de modelo interno
- Facilita evolución sin breaking changes

**5. Configuración Externalizada:**
```properties
spring.datasource.url=${DATABASE_URL}
jwt.secret=${JWT_SECRET}
```
- Mismo JAR funciona en dev/staging/prod cambiando variables entorno
- Credenciales fuera del código (`.env`, AWS Secrets Manager)

**6. Connection Pooling HikariCP:**
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.connection-timeout=30000
```
- Reutiliza conexiones DB
- Maneja picos de carga sin agotar connections

**7. Índices de Base de Datos:**
```sql
CREATE INDEX idx_productos_categoria ON productos(categoria_id);
CREATE INDEX idx_carrito_usuario ON carritos(usuario_id);
CREATE INDEX idx_ventas_usuario ON ventas(usuario_id);
CREATE INDEX idx_ventas_fecha ON ventas(fecha_venta);
```
- Queries frecuentes optimizadas
- Reduce latencia en búsquedas

**8. Paginación en Listados:**
```java
Page<Producto> findAll(Pageable pageable); // default 20 items
```
- Evita cargar miles de registros en memoria
- Reduce payload HTTP

**9. Lazy Loading JPA:**
```java
@OneToMany(fetch = FetchType.LAZY)
private List<CarritoItem> items;
```
- Carga datos solo cuando se acceden
- Reduce queries innecesarias

**10. Manejo Centralizado de Errores:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler { }
```
- Respuestas HTTP consistentes
- Logging centralizado para debugging

**11. Documentación OpenAPI/Swagger:**
```java
@OpenAPIDefinition(info = @Info(title = "Producto API", version = "1.0"))
```
- Contratos API autodocumentados
- Facilita integración frontend-backend

**12. Health Checks:**
```java
@RestController
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
```
- Monitoreo de disponibilidad servicios
- Integración con load balancers AWS ELB

**13. Logs Estructurados:**
```java
@Slf4j
public class VentaService {
    public Venta crear(VentaDTO dto) {
        log.info("Creando venta para usuario: {}, total: {}", 
            dto.getUsuarioId(), dto.getTotal());
        // ...
    }
}
```
- Trazabilidad de operaciones
- Debugging producción

**14. Transacciones Atómicas:**
```java
@Transactional
public Venta procesarVenta(VentaDTO dto) {
    // múltiples operaciones DB → commit/rollback automático
}
```
- Consistencia de datos garantizada
- Previene estados inconsistentes

**15. Límite de Recursos JVM:**
```bash
java -Xms256m -Xmx512m -jar usuario-service.jar
```
- Previene OutOfMemory en instancias pequeñas (EC2 t2.micro)
- Memoria predecible por servicio

---

