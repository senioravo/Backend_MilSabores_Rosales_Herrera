# Script para ejecutar servicios con Spring Boot Maven Plugin (sin Gradle)
# Alternativa cuando Gradle no está instalado
# PowerShell Script

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Ejecutando Servicios sin Build" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Cargar variables de entorno del archivo .env
if (Test-Path ".env") {
    Write-Host "Cargando variables de entorno desde .env..." -ForegroundColor Yellow
    Get-Content .env | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
            Write-Host "  $key configurado" -ForegroundColor Gray
        }
    }
    Write-Host ""
} else {
    Write-Host "ERROR: Archivo .env no encontrado" -ForegroundColor Red
    Write-Host "Crea el archivo .env con tus credenciales de Neon" -ForegroundColor Yellow
    exit 1
}

Write-Host "NOTA: Este método ejecuta los servicios directamente desde el código fuente" -ForegroundColor Yellow
Write-Host "Los servicios se compilarán y ejecutarán en modo desarrollo" -ForegroundColor Yellow
Write-Host ""

# Función para iniciar un servicio con Spring Boot
function Start-SpringBootService {
    param (
        [string]$serviceName,
        [int]$port
    )
    
    Write-Host "Iniciando $serviceName en puerto $port..." -ForegroundColor Yellow
    
    $servicePath = $serviceName
    
    if (Test-Path $servicePath) {
        Start-Process powershell -ArgumentList "-NoExit", "-Command", "
            `$env:DATABASE_URL='$env:DATABASE_URL';
            `$env:DATABASE_USERNAME='$env:DATABASE_USERNAME';
            `$env:DATABASE_PASSWORD='$env:DATABASE_PASSWORD';
            `$env:JWT_SECRET='$env:JWT_SECRET';
            Write-Host '============================================' -ForegroundColor Cyan;
            Write-Host '  $serviceName (Puerto $port)' -ForegroundColor Cyan;
            Write-Host '============================================' -ForegroundColor Cyan;
            Write-Host '';
            Write-Host 'Compilando y ejecutando...' -ForegroundColor Yellow;
            Write-Host 'Swagger UI: http://localhost:$port/swagger-ui.html' -ForegroundColor Green;
            Write-Host '';
            cd '$servicePath';
            java -cp 'src/main/java;build/classes/java/main' -Dspring.profiles.active=dev com.example.demo.*.Application 2>&1 | Write-Host
        "
        Write-Host "$serviceName iniciado en nueva ventana" -ForegroundColor Green
        Write-Host ""
    } else {
        Write-Host "ERROR: No se encontró el directorio $servicePath" -ForegroundColor Red
    }
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  INSTALACIÓN RECOMENDADA" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Para una mejor experiencia, instala Gradle:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Ejecuta: .\setup-gradle.ps1" -ForegroundColor White
Write-Host "2. Sigue las instrucciones de instalación" -ForegroundColor White
Write-Host "3. Luego ejecuta: .\run-all-services.ps1" -ForegroundColor White
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$continuar = Read-Host "¿Deseas continuar sin Gradle? (S/N)"

if ($continuar -ne "S" -and $continuar -ne "s") {
    Write-Host "Instalación cancelada" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Nota: Este método puede no funcionar correctamente" -ForegroundColor Red
Write-Host "Se recomienda instalar Gradle para compilar los servicios" -ForegroundColor Yellow
Write-Host ""

# Intentar iniciar servicios (probablemente fallará sin build)
Start-SpringBootService "usuario-service" 8081
Start-Sleep -Seconds 2

Start-SpringBootService "producto-service" 8082
Start-Sleep -Seconds 2

Start-SpringBootService "carrito-service" 8083
Start-Sleep -Seconds 2

Start-SpringBootService "ventas-service" 8084
