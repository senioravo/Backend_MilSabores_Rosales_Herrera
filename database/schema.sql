-- ============================================================================
-- Script de creación de base de datos para Mil Sabores
-- PostgreSQL (Neon)
-- Base de datos: neondb
-- Versión: 1.0
-- Fecha: 2025-11-27
-- ============================================================================

-- NOTA: Este script está diseñado para ejecutarse en la base de datos "neondb"
-- No es necesario crear una nueva base de datos, usaremos la existente

-- ============================================================================
-- ELIMINAR TABLAS EXISTENTES (Si es necesario reiniciar)
-- ============================================================================
-- Descomentar estas líneas solo si necesitas recrear todas las tablas

-- DROP TABLE IF EXISTS detalle_ventas CASCADE;
-- DROP TABLE IF EXISTS ventas CASCADE;
-- DROP TABLE IF EXISTS carrito_items CASCADE;
-- DROP TABLE IF EXISTS producto_etiquetas CASCADE;
-- DROP TABLE IF EXISTS producto_tamanos CASCADE;
-- DROP TABLE IF EXISTS productos CASCADE;
-- DROP TABLE IF EXISTS categorias CASCADE;
-- DROP TABLE IF EXISTS usuarios CASCADE;

-- ============================================================================
-- TABLA: USUARIOS
-- Almacena información de usuarios registrados
-- ============================================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Constraints
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_nombre_length CHECK (LENGTH(nombre) >= 2)
);

-- Índices para optimizar búsquedas
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_usuarios_activo ON usuarios(activo);

-- Comentarios de documentación
COMMENT ON TABLE usuarios IS 'Almacena información de usuarios registrados en el sistema';
COMMENT ON COLUMN usuarios.id IS 'Identificador único del usuario';
COMMENT ON COLUMN usuarios.email IS 'Correo electrónico único del usuario';
COMMENT ON COLUMN usuarios.password IS 'Contraseña del usuario (debe estar hasheada en producción)';
COMMENT ON COLUMN usuarios.fecha_registro IS 'Fecha y hora de registro del usuario';
COMMENT ON COLUMN usuarios.activo IS 'Indica si el usuario está activo en el sistema';

-- ============================================================================
-- TABLA: CATEGORIAS
-- Almacena las categorías de productos
-- ============================================================================

CREATE TABLE IF NOT EXISTS categorias (
    id VARCHAR(10) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    imagen VARCHAR(255),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Constraints
    CONSTRAINT chk_categoria_id_format CHECK (id ~ '^[A-Z]{2,10}$'),
    CONSTRAINT chk_nombre_categoria_length CHECK (LENGTH(nombre) >= 3)
);

-- Índice para búsquedas
CREATE INDEX IF NOT EXISTS idx_categorias_activo ON categorias(activo);

-- Comentarios de documentación
COMMENT ON TABLE categorias IS 'Categorías de productos de la pastelería';
COMMENT ON COLUMN categorias.id IS 'Código único de la categoría (ej: TC, TT, PI)';
COMMENT ON COLUMN categorias.nombre IS 'Nombre descriptivo de la categoría';
COMMENT ON COLUMN categorias.descripcion IS 'Descripción detallada de la categoría';
COMMENT ON COLUMN categorias.imagen IS 'Ruta o nombre del archivo de imagen de la categoría';

-- ============================================================================
-- TABLA: PRODUCTOS
-- Almacena información de productos disponibles
-- ============================================================================

CREATE TABLE IF NOT EXISTS productos (
    code VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    categoria_id VARCHAR(10) NOT NULL,
    tipo_forma VARCHAR(50),
    precio_clp INTEGER NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    personalizable BOOLEAN NOT NULL DEFAULT FALSE,
    max_msg_chars INTEGER DEFAULT 0,
    descripcion TEXT,
    imagen VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_precio_positivo CHECK (precio_clp >= 0),
    CONSTRAINT chk_stock_no_negativo CHECK (stock >= 0),
    CONSTRAINT chk_max_msg_chars_positivo CHECK (max_msg_chars >= 0),
    CONSTRAINT chk_code_format CHECK (code ~ '^[A-Z]{2,5}[0-9]{3,5}$'),
    
    -- Foreign Keys
    CONSTRAINT fk_productos_categoria 
        FOREIGN KEY (categoria_id) 
        REFERENCES categorias(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE
);

-- Índices para optimizar búsquedas
CREATE INDEX IF NOT EXISTS idx_productos_categoria ON productos(categoria_id);
CREATE INDEX IF NOT EXISTS idx_productos_activo ON productos(activo);
CREATE INDEX IF NOT EXISTS idx_productos_stock ON productos(stock);
CREATE INDEX IF NOT EXISTS idx_productos_precio ON productos(precio_clp);
CREATE INDEX IF NOT EXISTS idx_productos_nombre ON productos USING gin(to_tsvector('spanish', nombre));

-- Comentarios de documentación
COMMENT ON TABLE productos IS 'Productos disponibles en la pastelería';
COMMENT ON COLUMN productos.code IS 'Código único del producto (ej: TC001, TT002)';
COMMENT ON COLUMN productos.nombre IS 'Nombre del producto';
COMMENT ON COLUMN productos.tipo_forma IS 'Forma del producto (circular, cuadrada, etc.)';
COMMENT ON COLUMN productos.precio_clp IS 'Precio del producto en pesos chilenos';
COMMENT ON COLUMN productos.stock IS 'Cantidad disponible en inventario';
COMMENT ON COLUMN productos.personalizable IS 'Indica si el producto puede personalizarse';
COMMENT ON COLUMN productos.max_msg_chars IS 'Máximo de caracteres para mensaje personalizado';

-- ============================================================================
-- TABLA: PRODUCTO_TAMANOS
-- Almacena los tamaños disponibles para cada producto
-- ============================================================================

CREATE TABLE IF NOT EXISTS producto_tamanos (
    id BIGSERIAL PRIMARY KEY,
    producto_code VARCHAR(20) NOT NULL,
    tamano VARCHAR(100) NOT NULL,
    orden INTEGER DEFAULT 0,
    
    -- Constraints
    CONSTRAINT uq_producto_tamano UNIQUE (producto_code, tamano),
    
    -- Foreign Keys
    CONSTRAINT fk_tamano_producto 
        FOREIGN KEY (producto_code) 
        REFERENCES productos(code) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_producto_tamanos_code ON producto_tamanos(producto_code);

-- Comentarios
COMMENT ON TABLE producto_tamanos IS 'Tamaños disponibles para cada producto';
COMMENT ON COLUMN producto_tamanos.tamano IS 'Descripción del tamaño (ej: S (8 porciones))';
COMMENT ON COLUMN producto_tamanos.orden IS 'Orden de presentación del tamaño';

-- ============================================================================
-- TABLA: PRODUCTO_ETIQUETAS
-- Almacena etiquetas/tags para cada producto
-- ============================================================================

CREATE TABLE IF NOT EXISTS producto_etiquetas (
    id BIGSERIAL PRIMARY KEY,
    producto_code VARCHAR(20) NOT NULL,
    etiqueta VARCHAR(50) NOT NULL,
    
    -- Constraints
    CONSTRAINT uq_producto_etiqueta UNIQUE (producto_code, etiqueta),
    CONSTRAINT chk_etiqueta_format CHECK (etiqueta ~ '^[a-z0-9_]+$'),
    
    -- Foreign Keys
    CONSTRAINT fk_etiqueta_producto 
        FOREIGN KEY (producto_code) 
        REFERENCES productos(code) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_producto_etiquetas_code ON producto_etiquetas(producto_code);
CREATE INDEX IF NOT EXISTS idx_producto_etiquetas_etiqueta ON producto_etiquetas(etiqueta);

-- Comentarios
COMMENT ON TABLE producto_etiquetas IS 'Etiquetas/tags descriptivas de productos';
COMMENT ON COLUMN producto_etiquetas.etiqueta IS 'Etiqueta del producto (ej: tradicional, chocolate, vegana)';

-- ============================================================================
-- TABLA: CARRITO_ITEMS
-- Almacena items del carrito de compras de cada usuario
-- ============================================================================

CREATE TABLE IF NOT EXISTS carrito_items (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    producto_code VARCHAR(20) NOT NULL,
    producto_nombre VARCHAR(200) NOT NULL,
    precio_clp INTEGER NOT NULL,
    producto_imagen VARCHAR(255),
    cantidad INTEGER NOT NULL,
    stock_disponible INTEGER,
    fecha_agregado TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_cantidad_positiva CHECK (cantidad >= 1),
    CONSTRAINT chk_precio_carrito_positivo CHECK (precio_clp >= 0),
    CONSTRAINT uq_usuario_producto UNIQUE (usuario_id, producto_code),
    
    -- Foreign Keys
    CONSTRAINT fk_carrito_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_carrito_producto 
        FOREIGN KEY (producto_code) 
        REFERENCES productos(code) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
);

-- Índices para optimizar búsquedas
CREATE INDEX IF NOT EXISTS idx_carrito_usuario ON carrito_items(usuario_id);
CREATE INDEX IF NOT EXISTS idx_carrito_producto ON carrito_items(producto_code);
CREATE INDEX IF NOT EXISTS idx_carrito_fecha ON carrito_items(fecha_agregado);

-- Comentarios
COMMENT ON TABLE carrito_items IS 'Items del carrito de compras por usuario';
COMMENT ON COLUMN carrito_items.cantidad IS 'Cantidad del producto en el carrito';
COMMENT ON COLUMN carrito_items.stock_disponible IS 'Stock disponible al momento de agregar al carrito';
COMMENT ON COLUMN carrito_items.precio_clp IS 'Precio del producto al momento de agregarlo al carrito';

-- ============================================================================
-- TABLA: VENTAS
-- Almacena las ventas realizadas
-- ============================================================================

CREATE TABLE IF NOT EXISTS ventas (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    usuario_nombre VARCHAR(100),
    usuario_email VARCHAR(100),
    subtotal INTEGER NOT NULL,
    iva INTEGER NOT NULL,
    total INTEGER NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    transbank_token VARCHAR(255),
    transbank_order_id VARCHAR(255),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_venta_subtotal_positivo CHECK (subtotal >= 0),
    CONSTRAINT chk_venta_iva_positivo CHECK (iva >= 0),
    CONSTRAINT chk_venta_total_positivo CHECK (total > 0),
    CONSTRAINT chk_venta_estado CHECK (estado IN ('PENDIENTE', 'PROCESANDO', 'COMPLETADA', 'RECHAZADA', 'CANCELADA')),
    
    -- Foreign Keys
    CONSTRAINT fk_venta_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_ventas_usuario ON ventas(usuario_id);
CREATE INDEX IF NOT EXISTS idx_ventas_estado ON ventas(estado);
CREATE INDEX IF NOT EXISTS idx_ventas_fecha ON ventas(fecha_creacion);

-- Comentarios
COMMENT ON TABLE ventas IS 'Ventas realizadas en el sistema';
COMMENT ON COLUMN ventas.subtotal IS 'Subtotal de la venta sin IVA';
COMMENT ON COLUMN ventas.iva IS 'Monto del IVA (19%)';
COMMENT ON COLUMN ventas.total IS 'Total de la venta (subtotal + IVA)';
COMMENT ON COLUMN ventas.estado IS 'Estado de la venta: PENDIENTE, PROCESANDO, COMPLETADA, RECHAZADA, CANCELADA';
COMMENT ON COLUMN ventas.transbank_token IS 'Token de transacción de Transbank';
COMMENT ON COLUMN ventas.transbank_order_id IS 'ID de orden de compra de Transbank';

-- ============================================================================
-- TABLA: DETALLE_VENTAS
-- Almacena el detalle de productos vendidos en cada venta
-- ============================================================================

CREATE TABLE IF NOT EXISTS detalle_ventas (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    producto_code VARCHAR(20) NOT NULL,
    producto_nombre VARCHAR(200) NOT NULL,
    producto_imagen VARCHAR(255),
    cantidad INTEGER NOT NULL,
    precio_unitario INTEGER NOT NULL,
    subtotal INTEGER NOT NULL,
    
    -- Constraints
    CONSTRAINT chk_detalle_cantidad_positiva CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio_positivo CHECK (precio_unitario >= 0),
    CONSTRAINT chk_detalle_subtotal_positivo CHECK (subtotal >= 0),
    
    -- Foreign Keys
    CONSTRAINT fk_detalle_venta 
        FOREIGN KEY (venta_id) 
        REFERENCES ventas(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_detalle_producto 
        FOREIGN KEY (producto_code) 
        REFERENCES productos(code) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_detalle_ventas_venta ON detalle_ventas(venta_id);
CREATE INDEX IF NOT EXISTS idx_detalle_ventas_producto ON detalle_ventas(producto_code);

-- Comentarios
COMMENT ON TABLE detalle_ventas IS 'Detalle de productos incluidos en cada venta';
COMMENT ON COLUMN detalle_ventas.cantidad IS 'Cantidad del producto vendido';
COMMENT ON COLUMN detalle_ventas.precio_unitario IS 'Precio unitario al momento de la venta';
COMMENT ON COLUMN detalle_ventas.subtotal IS 'Subtotal del item (cantidad * precio_unitario)';

-- ============================================================================
-- TRIGGERS
-- Actualización automática de fecha_actualizacion
-- ============================================================================

-- Función para actualizar fecha_actualizacion
CREATE OR REPLACE FUNCTION actualizar_fecha_actualizacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para productos
CREATE TRIGGER trigger_productos_actualizacion
    BEFORE UPDATE ON productos
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_fecha_actualizacion();

-- Trigger para carrito_items
CREATE TRIGGER trigger_carrito_actualizacion
    BEFORE UPDATE ON carrito_items
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_fecha_actualizacion();

-- Trigger para ventas
CREATE TRIGGER trigger_ventas_actualizacion
    BEFORE UPDATE ON ventas
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_fecha_actualizacion();

-- ============================================================================
-- VISTAS ÚTILES
-- ============================================================================

-- Vista de productos con información de categoría
CREATE OR REPLACE VIEW v_productos_completos AS
SELECT 
    p.code,
    p.nombre,
    p.categoria_id,
    c.nombre AS categoria_nombre,
    c.descripcion AS categoria_descripcion,
    p.tipo_forma,
    p.precio_clp,
    p.stock,
    p.personalizable,
    p.max_msg_chars,
    p.descripcion,
    p.imagen,
    p.activo,
    p.fecha_creacion,
    p.fecha_actualizacion
FROM productos p
INNER JOIN categorias c ON p.categoria_id = c.id;

-- Vista de carrito con totales
CREATE OR REPLACE VIEW v_carrito_totales AS
SELECT 
    c.usuario_id,
    u.nombre AS usuario_nombre,
    u.email AS usuario_email,
    COUNT(c.id) AS total_items_distintos,
    SUM(c.cantidad) AS total_items,
    SUM(c.precio_clp * c.cantidad) AS total_precio
FROM carrito_items c
INNER JOIN usuarios u ON c.usuario_id = u.id
GROUP BY c.usuario_id, u.nombre, u.email;

-- Vista de productos más populares en carritos
CREATE OR REPLACE VIEW v_productos_populares AS
SELECT 
    p.code,
    p.nombre,
    p.categoria_id,
    COUNT(c.id) AS veces_en_carrito,
    SUM(c.cantidad) AS cantidad_total
FROM productos p
LEFT JOIN carrito_items c ON p.code = c.producto_code
GROUP BY p.code, p.nombre, p.categoria_id
ORDER BY veces_en_carrito DESC, cantidad_total DESC;

-- Vista de ventas con totales por estado
CREATE OR REPLACE VIEW v_ventas_resumen AS
SELECT 
    estado,
    COUNT(*) AS total_ventas,
    SUM(total) AS monto_total,
    AVG(total) AS promedio_venta,
    MIN(total) AS venta_minima,
    MAX(total) AS venta_maxima
FROM ventas
GROUP BY estado;

-- Vista de productos más vendidos
CREATE OR REPLACE VIEW v_productos_mas_vendidos AS
SELECT 
    p.code,
    p.nombre,
    p.categoria_id,
    c.nombre AS categoria_nombre,
    COUNT(dv.id) AS total_ventas,
    SUM(dv.cantidad) AS unidades_vendidas,
    SUM(dv.subtotal) AS ingresos_totales
FROM productos p
LEFT JOIN detalle_ventas dv ON p.code = dv.producto_code
LEFT JOIN categorias c ON p.categoria_id = c.id
GROUP BY p.code, p.nombre, p.categoria_id, c.nombre
ORDER BY unidades_vendidas DESC;

-- ============================================================================
-- FUNCIONES ÚTILES
-- ============================================================================

-- Función para obtener el subtotal de un item del carrito
CREATE OR REPLACE FUNCTION obtener_subtotal_item(p_item_id BIGINT)
RETURNS INTEGER AS $$
DECLARE
    v_subtotal INTEGER;
BEGIN
    SELECT precio_clp * cantidad INTO v_subtotal
    FROM carrito_items
    WHERE id = p_item_id;
    
    RETURN COALESCE(v_subtotal, 0);
END;
$$ LANGUAGE plpgsql;

-- Función para limpiar carritos antiguos (más de 30 días sin actualizar)
CREATE OR REPLACE FUNCTION limpiar_carritos_antiguos()
RETURNS INTEGER AS $$
DECLARE
    v_eliminados INTEGER;
BEGIN
    DELETE FROM carrito_items
    WHERE fecha_actualizacion < CURRENT_TIMESTAMP - INTERVAL '30 days';
    
    GET DIAGNOSTICS v_eliminados = ROW_COUNT;
    RETURN v_eliminados;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- DATOS INICIALES
-- ============================================================================

-- Insertar Categorías
INSERT INTO categorias (id, nombre, descripcion, imagen, activo) VALUES
('TC', 'Tortas Cuadradas', 'Tortas de forma cuadrada en varios tamaños, perfectas para celebraciones familiares.', 'TC.png', TRUE),
('TT', 'Tortas Circulares', 'Tortas redondas clásicas para toda ocasión, desde cumpleaños hasta bodas.', 'TT.png', TRUE),
('PI', 'Postres Individuales', 'Porciones individuales para llevar o compartir, perfectas para el café.', 'PI.png', TRUE),
('PSA', 'Productos Sin Azúcar', 'Opciones endulzadas naturalmente o sin azúcar añadida, para cuidar tu salud.', 'PSA.png', TRUE),
('PT', 'Pastelería Tradicional', 'Clásicos de la pastelería chilena que nunca fallan.', 'PT.png', TRUE),
('PG', 'Productos Sin Gluten', 'Preparaciones libres de gluten, sin sacrificar el sabor.', 'PG.png', TRUE),
('PV', 'Productos Veganos', 'Opciones 100% libres de productos animales, deliciosas y conscientes.', 'PV.png', TRUE),
('TE', 'Tortas Especiales', 'Decoraciones y temáticas especiales para celebrar momentos únicos.', 'TE.png', TRUE)
ON CONFLICT (id) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    imagen = EXCLUDED.imagen,
    activo = EXCLUDED.activo;

-- Insertar Productos iniciales
INSERT INTO productos (code, nombre, categoria_id, tipo_forma, precio_clp, stock, personalizable, max_msg_chars, descripcion, imagen, activo) VALUES
('TC001', 'Torta Cuadrada de Chocolate', 'TC', 'cuadrada', 45000, 10, TRUE, 50, 'Deliciosa torta de chocolate con ganache y toque de avellanas. Ideal para personalizar con mensaje.', 'TC001.png', TRUE),
('TC002', 'Torta Cuadrada de Frutas', 'TC', 'cuadrada', 50000, 8, TRUE, 50, 'Bizcocho de vainilla con frutas frescas y crema chantilly.', 'TC002.png', TRUE),
('TT001', 'Torta Circular de Vainilla', 'TT', 'circular', 40000, 12, TRUE, 50, 'Vainilla clásica rellena con crema pastelera y glaseado dulce.', 'TT001.png', TRUE),
('TT002', 'Torta Circular de Manjar', 'TT', 'circular', 42000, 9, TRUE, 50, 'Clásica torta chilena con manjar y nueces.', 'TT002.png', TRUE),
('PI001', 'Mousse de Chocolate', 'PI', NULL, 5000, 40, FALSE, 0, 'Postre cremoso con chocolate de alta calidad.', 'PI001.png', TRUE),
('PI002', 'Tiramisú Clásico', 'PI', NULL, 5500, 36, FALSE, 0, 'Café, mascarpone y cacao en un equilibrio perfecto.', 'PI002.png', TRUE),
('PSA001', 'Torta Sin Azúcar de Naranja', 'PSA', 'circular', 48000, 7, TRUE, 50, 'Endulzada naturalmente para quienes buscan opciones más saludables.', 'PSA001.png', TRUE),
('PSA002', 'Cheesecake Sin Azúcar', 'PSA', 'circular', 47000, 6, TRUE, 50, 'Suave y cremoso, ideal para disfrutar sin culpa.', 'PSA002.png', TRUE),
('PT001', 'Empanada de Manzana', 'PT', NULL, 3000, 50, FALSE, 0, 'Rellena de manzanas especiadas, perfecta para el desayuno o merienda.', 'PT001.png', TRUE),
('PT002', 'Tarta de Santiago', 'PT', 'circular', 6000, 22, FALSE, 0, 'Clásica tarta de almendras, azúcar y huevos.', 'PT002.png', TRUE),
('PG001', 'Brownie Sin Gluten', 'PG', 'cuadrada', 4000, 35, FALSE, 0, 'Denso y sabroso, libre de gluten.', 'PG001.png', TRUE),
('PG002', 'Pan Sin Gluten', 'PG', NULL, 3500, 28, FALSE, 0, 'Suave y esponjoso, ideal para sándwiches.', 'PG002.png', TRUE),
('PV001', 'Torta Vegana de Chocolate', 'PV', 'circular', 50000, 6, TRUE, 50, 'Húmeda y deliciosa, sin ingredientes de origen animal.', 'PV001.png', TRUE),
('PV002', 'Galletas Veganas de Avena', 'PV', NULL, 4500, 40, FALSE, 0, 'Crujientes y sabrosas, perfectas para colación.', 'PV002.png', TRUE),
('TE001', 'Torta Especial de Cumpleaños', 'TE', 'circular', 55000, 7, TRUE, 50, 'Pensada para celebrar: admite decoraciones temáticas y mensaje.', 'TE001.png', TRUE),
('TE002', 'Torta Especial de Boda', 'TE', 'circular', 60000, 4, TRUE, 50, 'Elegante y memorable; lista para personalizar.', 'TE002.png', TRUE)
ON CONFLICT (code) DO NOTHING;

-- Insertar Tamaños de Productos
INSERT INTO producto_tamanos (producto_code, tamano) VALUES
('TC001', 'S (8 porciones)'),
('TC001', 'M (12 porciones)'),
('TC001', 'L (20 porciones)'),
('TC002', 'S (8 porciones)'),
('TC002', 'M (12 porciones)'),
('TC002', 'L (20 porciones)'),
('TT001', 'S (8 porciones)'),
('TT001', 'M (12 porciones)'),
('TT001', 'L (20 porciones)'),
('TT002', 'S (8 porciones)'),
('TT002', 'M (12 porciones)'),
('TT002', 'L (20 porciones)'),
('PI001', 'unidad'),
('PI002', 'unidad'),
('PSA001', 'S (8 porciones)'),
('PSA001', 'M (12 porciones)'),
('PSA002', 'S (8 porciones)'),
('PSA002', 'M (12 porciones)'),
('PT001', 'unidad'),
('PT002', 'S (8 porciones)'),
('PG001', 'unidad'),
('PG002', 'unidad'),
('PV001', 'S (8 porciones)'),
('PV001', 'M (12 porciones)'),
('PV002', 'unidad'),
('TE001', 'S (8 porciones)'),
('TE001', 'M (12 porciones)'),
('TE001', 'L (20 porciones)'),
('TE002', 'M (12 porciones)'),
('TE002', 'L (20 porciones)');

-- Insertar Etiquetas de Productos
INSERT INTO producto_etiquetas (producto_code, etiqueta) VALUES
('TC001', 'tradicional'),
('TC002', 'tradicional'),
('TC002', 'frutas'),
('TT001', 'tradicional'),
('TT002', 'tradicional'),
('PI001', 'chocolate'),
('PI002', 'clasico'),
('PSA001', 'sin_azucar'),
('PSA002', 'sin_azucar'),
('PT001', 'tradicional'),
('PT002', 'tradicional'),
('PG001', 'sin_gluten'),
('PG001', 'chocolate'),
('PG002', 'sin_gluten'),
('PV001', 'vegana'),
('PV001', 'chocolate'),
('PV002', 'vegana'),
('TE001', 'especial'),
('TE001', 'cumpleanos'),
('TE002', 'especial'),
('TE002', 'boda')
ON CONFLICT (producto_code, etiqueta) DO NOTHING;

-- ============================================================================
-- USUARIOS DE PRUEBA
-- ============================================================================

-- Usuario de prueba principal (contraseña: password123)
INSERT INTO usuarios (nombre, email, password, activo) VALUES
('Usuario Demo', 'demo@milsabores.cl', 'password123', TRUE),
('Juan Pérez', 'juan.perez@example.com', 'password123', TRUE),
('María González', 'maria.gonzalez@example.com', 'password123', TRUE),
('Admin Sistema', 'admin@milsabores.cl', 'admin123', TRUE)
ON CONFLICT (email) DO NOTHING;

-- ============================================================================
-- ESTADÍSTICAS Y VERIFICACIÓN
-- ============================================================================

-- Mostrar estadísticas de las tablas creadas
DO $$
DECLARE
    v_usuarios INTEGER;
    v_categorias INTEGER;
    v_productos INTEGER;
    v_carrito INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_usuarios FROM usuarios;
    SELECT COUNT(*) INTO v_categorias FROM categorias;
    SELECT COUNT(*) INTO v_productos FROM productos;
    SELECT COUNT(*) INTO v_carrito FROM carrito_items;
    
    RAISE NOTICE '';
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'BASE DE DATOS MIL SABORES - CREADA EXITOSAMENTE';
    RAISE NOTICE '============================================================';
    RAISE NOTICE 'Tablas creadas: 8';
    RAISE NOTICE '  - usuarios: % registros', v_usuarios;
    RAISE NOTICE '  - categorias: % registros', v_categorias;
    RAISE NOTICE '  - productos: % registros', v_productos;
    RAISE NOTICE '  - producto_tamanos';
    RAISE NOTICE '  - producto_etiquetas';
    RAISE NOTICE '  - carrito_items: % registros', v_carrito;
    RAISE NOTICE '  - ventas';
    RAISE NOTICE '  - detalle_ventas';
    RAISE NOTICE '';
    RAISE NOTICE 'Vistas creadas: 5';
    RAISE NOTICE '  - v_productos_completos';
    RAISE NOTICE '  - v_carrito_totales';
    RAISE NOTICE '  - v_productos_populares';
    RAISE NOTICE '  - v_ventas_resumen';
    RAISE NOTICE '  - v_productos_mas_vendidos';
    RAISE NOTICE '';
    RAISE NOTICE 'Funciones creadas: 3';
    RAISE NOTICE '  - actualizar_fecha_actualizacion()';
    RAISE NOTICE '  - obtener_subtotal_item()';
    RAISE NOTICE '  - limpiar_carritos_antiguos()';
    RAISE NOTICE '';
    RAISE NOTICE 'Estado: LISTO PARA USAR';
    RAISE NOTICE '============================================================';
    RAISE NOTICE '';
END $$;

-- ============================================================================
-- CONSULTAS DE VERIFICACIÓN ÚTILES
-- ============================================================================

-- Verificar estructura de tablas
-- SELECT table_name, table_type FROM information_schema.tables 
-- WHERE table_schema = 'public' ORDER BY table_name;

-- Verificar índices
-- SELECT tablename, indexname FROM pg_indexes 
-- WHERE schemaname = 'public' ORDER BY tablename, indexname;

-- Verificar restricciones
-- SELECT conname, conrelid::regclass AS table_name, contype 
-- FROM pg_constraint WHERE connamespace = 'public'::regnamespace;

-- Listar todas las categorías
-- SELECT * FROM categorias ORDER BY id;

-- Listar todos los productos
-- SELECT code, nombre, categoria_id, precio_clp, stock FROM productos ORDER BY categoria_id, code;

-- Ver productos con categoría completa
-- SELECT * FROM v_productos_completos WHERE activo = TRUE ORDER BY categoria_id;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
