# Script para probar la conexión a los microservicios
# PowerShell Script

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Test de Conexión - Microservicios" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$services = @(
    @{Name="Usuario Service"; Port=8081; Path="/api/usuarios"},
    @{Name="Producto Service"; Port=8082; Path="/api/productos"},
    @{Name="Carrito Service"; Port=8083; Path="/api/carrito"},
    @{Name="Ventas Service"; Port=8084; Path="/api/ventas"}
)

foreach ($service in $services) {
    Write-Host "Probando $($service.Name) (Puerto $($service.Port))..." -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)$($service.Path)" -Method GET -TimeoutSec 5 -UseBasicParsing
        Write-Host "  ✓ $($service.Name) - ACTIVO (Status: $($response.StatusCode))" -ForegroundColor Green
        Write-Host "    Swagger: http://localhost:$($service.Port)/swagger-ui.html" -ForegroundColor Cyan
    }
    catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 401 -or $_.Exception.Response.StatusCode.value__ -eq 403) {
            Write-Host "  ✓ $($service.Name) - ACTIVO (Requiere autenticación)" -ForegroundColor Yellow
            Write-Host "    Swagger: http://localhost:$($service.Port)/swagger-ui.html" -ForegroundColor Cyan
        }
        else {
            Write-Host "  ✗ $($service.Name) - NO DISPONIBLE" -ForegroundColor Red
            Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    Write-Host ""
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Test de Categorías (Producto Service)" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8082/api/categorias" -Method GET -UseBasicParsing
    $categorias = $response.Content | ConvertFrom-Json
    Write-Host "✓ Categorías encontradas: $($categorias.Count)" -ForegroundColor Green
    foreach ($cat in $categorias) {
        Write-Host "  - $($cat.id): $($cat.nombre)" -ForegroundColor Cyan
    }
}
catch {
    Write-Host "✗ No se pudieron obtener las categorías" -ForegroundColor Red
    Write-Host "  Asegúrate de que producto-service está ejecutándose" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Test completado." -ForegroundColor Green
