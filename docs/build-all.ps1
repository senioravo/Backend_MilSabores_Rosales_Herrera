# Script PowerShell para construir todos los microservicios
# Ejecutar desde la carpeta BackendMilSabores

Write-Host "🔨 Construyendo microservicios de Mil Sabores..." -ForegroundColor Cyan

# Usuario Service
Write-Host "`n📦 Construyendo Usuario Service..." -ForegroundColor Yellow
Set-Location usuario-service
$result = & ./gradlew.bat clean build
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Usuario Service construido exitosamente" -ForegroundColor Green
} else {
    Write-Host "✗ Error al construir Usuario Service" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

# Producto Service
Write-Host "`n📦 Construyendo Producto Service..." -ForegroundColor Yellow
Set-Location producto-service
$result = & ./gradlew.bat clean build
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Producto Service construido exitosamente" -ForegroundColor Green
} else {
    Write-Host "✗ Error al construir Producto Service" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

# Carrito Service
Write-Host "`n📦 Construyendo Carrito Service..." -ForegroundColor Yellow
Set-Location carrito-service
$result = & ./gradlew.bat clean build
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Carrito Service construido exitosamente" -ForegroundColor Green
} else {
    Write-Host "✗ Error al construir Carrito Service" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

Write-Host "`n✨ Todos los microservicios construidos exitosamente!" -ForegroundColor Green
Write-Host "`n📂 JARs generados en:" -ForegroundColor Cyan
Write-Host "   - usuario-service/build/libs/usuario-service-0.0.1-SNAPSHOT.jar"
Write-Host "   - producto-service/build/libs/producto-service-0.0.1-SNAPSHOT.jar"
Write-Host "   - carrito-service/build/libs/carrito-service-0.0.1-SNAPSHOT.jar"
