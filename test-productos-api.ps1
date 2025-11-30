# Script para probar la API de Productos
# PowerShell Script

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Test API de Productos - Mil Sabores" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8082/api"

# Función para hacer peticiones con formato
function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Url,
        [string]$Description,
        [object]$Body = $null
    )
    
    Write-Host "🔹 $Description" -ForegroundColor Yellow
    Write-Host "   Método: $Method" -ForegroundColor Gray
    Write-Host "   URL: $Url" -ForegroundColor Gray
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            ContentType = "application/json"
            UseBasicParsing = $true
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json)
            Write-Host "   Body: $($params.Body)" -ForegroundColor Gray
        }
        
        $response = Invoke-WebRequest @params
        $data = $response.Content | ConvertFrom-Json
        
        Write-Host "   ✅ Status: $($response.StatusCode)" -ForegroundColor Green
        
        if ($data -is [array]) {
            Write-Host "   📊 Resultados: $($data.Count) items" -ForegroundColor Cyan
            if ($data.Count -gt 0) {
                Write-Host "   Ejemplo:" -ForegroundColor Gray
                Write-Host "   $($data[0] | ConvertTo-Json -Depth 2)" -ForegroundColor DarkGray
            }
        } else {
            Write-Host "   📦 Resultado:" -ForegroundColor Cyan
            Write-Host "   $($data | ConvertTo-Json -Depth 2)" -ForegroundColor DarkGray
        }
    }
    catch {
        Write-Host "   ❌ Error: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            Write-Host "   Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        }
    }
    
    Write-Host ""
}

# 1. GET - Obtener todos los productos
Test-Endpoint -Method "GET" -Url "$baseUrl/productos" -Description "Obtener todos los productos"

# 2. GET - Obtener productos destacados
Test-Endpoint -Method "GET" -Url "$baseUrl/productos/destacados?limit=3" -Description "Obtener productos destacados (límite 3)"

# 3. GET - Buscar productos por nombre
Test-Endpoint -Method "GET" -Url "$baseUrl/productos/buscar?nombre=torta" -Description "Buscar productos por nombre (torta)"

# 4. GET - Obtener categorías
Write-Host "Obteniendo categorías para usar en pruebas..." -ForegroundColor Cyan
try {
    $categoriasResponse = Invoke-WebRequest -Uri "$baseUrl/categorias" -Method GET -UseBasicParsing
    $categorias = $categoriasResponse.Content | ConvertFrom-Json
    
    if ($categorias.Count -gt 0) {
        $primeraCategoria = $categorias[0]
        Write-Host "Primera categoría encontrada: $($primeraCategoria.nombre) (ID: $($primeraCategoria.id))" -ForegroundColor Green
        
        # 5. GET - Obtener productos por categoría
        Test-Endpoint -Method "GET" -Url "$baseUrl/productos/categoria/$($primeraCategoria.id)" -Description "Obtener productos de categoría '$($primeraCategoria.nombre)'"
    }
}
catch {
    Write-Host "No se pudieron obtener categorías: $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

# 6. GET - Obtener producto por código (ejemplo)
Write-Host "Intentando obtener producto con código TRT001..." -ForegroundColor Cyan
Test-Endpoint -Method "GET" -Url "$baseUrl/productos/TRT001" -Description "Obtener producto por código (TRT001)"

# 7. POST - Crear nuevo producto (ejemplo)
Write-Host "============================================" -ForegroundColor Magenta
Write-Host "  Prueba de CREACIÓN (POST)" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta
Write-Host ""

$nuevoProducto = @{
    code = "TEST" + (Get-Date -Format "HHmmss")
    nombre = "Producto de Prueba PowerShell"
    descripcion = "Producto creado desde script de prueba"
    precioCLP = 12000
    stock = 5
    categoriaId = 1
    imagen = "test.jpg"
    destacado = $false
    activo = $true
}

Test-Endpoint -Method "POST" -Url "$baseUrl/productos" -Description "Crear nuevo producto" -Body $nuevoProducto

# 8. PATCH - Actualizar stock (si tenemos un código)
Write-Host "============================================" -ForegroundColor Magenta
Write-Host "  Prueba de ACTUALIZACIÓN DE STOCK (PATCH)" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta
Write-Host ""

$codigoParaActualizar = $nuevoProducto.code
$nuevoStock = 10

Test-Endpoint -Method "PATCH" -Url "$baseUrl/productos/$codigoParaActualizar/stock?stock=$nuevoStock" -Description "Actualizar stock del producto $codigoParaActualizar a $nuevoStock"

# 9. PUT - Actualizar producto completo
Write-Host "============================================" -ForegroundColor Magenta
Write-Host "  Prueba de ACTUALIZACIÓN COMPLETA (PUT)" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta
Write-Host ""

$productoActualizado = @{
    code = $codigoParaActualizar
    nombre = "Producto ACTUALIZADO desde PowerShell"
    descripcion = "Descripción actualizada"
    precioCLP = 15000
    stock = 15
    categoriaId = 1
    imagen = "test-updated.jpg"
    destacado = $true
    activo = $true
}

Test-Endpoint -Method "PUT" -Url "$baseUrl/productos/$codigoParaActualizar" -Description "Actualizar producto completo" -Body $productoActualizado

# 10. DELETE - Eliminar producto
Write-Host "============================================" -ForegroundColor Red
Write-Host "  Prueba de ELIMINACIÓN (DELETE)" -ForegroundColor Red
Write-Host "============================================" -ForegroundColor Red
Write-Host ""

$confirmar = Read-Host "¿Deseas eliminar el producto de prueba $codigoParaActualizar? (s/n)"
if ($confirmar -eq "s" -or $confirmar -eq "S") {
    Test-Endpoint -Method "DELETE" -Url "$baseUrl/productos/$codigoParaActualizar" -Description "Eliminar producto $codigoParaActualizar"
} else {
    Write-Host "Eliminación cancelada." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Test completado" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Para más pruebas, accede a Swagger UI:" -ForegroundColor Green
Write-Host "http://localhost:8082/swagger-ui.html" -ForegroundColor Cyan
Write-Host ""
Write-Host "O usa la interfaz web de pruebas:" -ForegroundColor Green
Write-Host "http://localhost:5173/test-productos" -ForegroundColor Cyan
