# Script para ejecutar todos los microservicios de Mil Sabores
# PowerShell Script

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Iniciando Microservicios Mil Sabores" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Cargar variables de entorno del archivo .env
if (Test-Path ".env") {
    Write-Host "Cargando variables de entorno desde .env..." -ForegroundColor Yellow
    Get-Content .env | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
        }
    }
} else {
    Write-Host "ADVERTENCIA: Archivo .env no encontrado. Usando valores por defecto." -ForegroundColor Red
    Write-Host "Copia .env.example a .env y configura tus credenciales de Neon." -ForegroundColor Yellow
    Write-Host ""
}

# Verificar que Java está instalado
$javaVersion = java -version 2>&1 | Select-String "version"
if ($javaVersion) {
    Write-Host "Java detectado: $javaVersion" -ForegroundColor Green
} else {
    Write-Host "ERROR: Java no está instalado o no está en el PATH" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Construyendo proyectos con Gradle..." -ForegroundColor Yellow
Write-Host ""

# Refrescar PATH para detectar Gradle recién instalado
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

# Verificar si gradle está instalado globalmente
$gradleInstalled = Get-Command gradle -ErrorAction SilentlyContinue

if (-not $gradleInstalled) {
    Write-Host "ERROR: Gradle no esta instalado en el sistema" -ForegroundColor Red
    Write-Host "Instala Gradle desde: https://gradle.org/install/" -ForegroundColor Yellow
    Write-Host "O usa: choco install gradle" -ForegroundColor Yellow
    exit 1
}

Write-Host "Gradle detectado:" -ForegroundColor Green
gradle --version | Select-Object -First 1
Write-Host ""

# Construir cada servicio
$services = @("usuario-service", "producto-service", "carrito-service", "ventas-service")

foreach ($service in $services) {
    Write-Host "Construyendo $service..." -ForegroundColor Cyan
    Push-Location $service
    
    # Usar gradle global en lugar del wrapper
    gradle clean build -x test
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Falló la construcción de $service" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    Pop-Location
    Write-Host "$service construido exitosamente" -ForegroundColor Green
    Write-Host ""
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Iniciando servicios..." -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Función para iniciar un servicio en una nueva ventana
function Start-Service {
    param (
        [string]$serviceName,
        [int]$port
    )
    
    Write-Host "Iniciando $serviceName en puerto $port..." -ForegroundColor Yellow
    
    $jarPath = "$serviceName\build\libs\$serviceName-0.0.1-SNAPSHOT.jar"
    
    if (Test-Path $jarPath) {
        # Obtener las variables de entorno actuales
        $dbUrl = $env:DATABASE_URL
        $dbUser = $env:DATABASE_USERNAME
        $dbPass = $env:DATABASE_PASSWORD
        $jwtSecret = $env:JWT_SECRET
        
        Start-Process powershell -ArgumentList "-NoExit", "-Command", "
            Write-Host '============================================' -ForegroundColor Cyan;
            Write-Host '  $serviceName (Puerto $port)' -ForegroundColor Cyan;
            Write-Host '============================================' -ForegroundColor Cyan;
            Write-Host '';
            Write-Host 'Swagger UI: http://localhost:$port/swagger-ui.html' -ForegroundColor Green;
            Write-Host 'API Docs: http://localhost:$port/v3/api-docs' -ForegroundColor Green;
            Write-Host '';
            `$env:DATABASE_URL='$dbUrl';
            `$env:DATABASE_USERNAME='$dbUser';
            `$env:DATABASE_PASSWORD='$dbPass';
            `$env:JWT_SECRET='$jwtSecret';
            java -jar '$jarPath'
        "
        Write-Host "$serviceName iniciado en nueva ventana" -ForegroundColor Green
    } else {
        Write-Host "ERROR: JAR no encontrado para $serviceName" -ForegroundColor Red
        Write-Host "Ruta esperada: $jarPath" -ForegroundColor Yellow
    }
}

# Iniciar cada servicio
Start-Service "usuario-service" 8081
Start-Sleep -Seconds 3

Start-Service "producto-service" 8082
Start-Sleep -Seconds 3

Start-Service "carrito-service" 8083
Start-Sleep -Seconds 3

Start-Service "ventas-service" 8084

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Todos los servicios iniciados!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "URLs de acceso:" -ForegroundColor Yellow
Write-Host "  Usuario Service:  http://localhost:8081/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  Producto Service: http://localhost:8082/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  Carrito Service:  http://localhost:8083/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  Ventas Service:   http://localhost:8084/swagger-ui.html" -ForegroundColor Cyan
Write-Host ""
Write-Host "Presiona Ctrl+C en cada ventana para detener los servicios" -ForegroundColor Yellow
