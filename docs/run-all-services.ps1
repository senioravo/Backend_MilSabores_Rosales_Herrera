# Script para ejecutar todos los microservicios de Mil Sabores
# PowerShell Script
#
# Uso rapido:
#   .\run-all-services.ps1              -> build incremental (paralelo) + arranque
#   .\run-all-services.ps1 -SkipBuild   -> no compila, solo (re)lanza los JARs ya construidos
#   .\run-all-services.ps1 -Clean       -> fuerza "clean build" en los 6 proyectos

param(
    [switch]$SkipBuild,
    [switch]$Clean
)

# Raíz del repo backend (este script vive en docs/)
$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$envFile = Join-Path $root ".env"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Iniciando Microservicios Mil Sabores" -ForegroundColor Cyan
Write-Host "  Raiz: $root" -ForegroundColor DarkGray
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Cargar variables de entorno del archivo .env en la raiz del backend
if (Test-Path $envFile) {
    Write-Host "Cargando variables de entorno desde .env..." -ForegroundColor Yellow
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
        }
    }
} else {
    Write-Host "ADVERTENCIA: Archivo .env no encontrado en $root" -ForegroundColor Red
    Write-Host "Copia .env.example a .env y configura Neon + AZURE_* (ver docs/GUIA-ENTORNO-LOCAL-EP1.md)." -ForegroundColor Yellow
    Write-Host ""
}

if (-not $env:FRONTEND_URL) {
    $env:FRONTEND_URL = "http://localhost:5173"
}

# Verificar que Java está instalado
$javaVersion = java -version 2>&1 | Select-String "version"
if ($javaVersion) {
    Write-Host "Java detectado: $javaVersion" -ForegroundColor Green
} else {
    Write-Host "ERROR: Java no está instalado o no está en el PATH" -ForegroundColor Red
    exit 1
}

$services = @("usuario-service", "producto-service", "carrito-service", "ventas-service", "api-gateway", "bff")

if ($SkipBuild) {
    Write-Host ""
    Write-Host "-SkipBuild: se omite la compilacion, se usan los JARs ya construidos." -ForegroundColor Yellow
} else {
    Write-Host ""
    Write-Host "Construyendo los 6 proyectos en paralelo..." -ForegroundColor Yellow
    Write-Host ""

    # Cada servicio trae su propio wrapper (gradlew.bat + gradle-wrapper.jar) y
    # corre en su propio proceso, asi que lanzar los 6 builds en paralelo con
    # Start-Job es seguro (no comparten estado) y es varias veces mas rapido
    # que compilarlos uno por uno. Sin -Clean se deja el build incremental de
    # Gradle hacer su trabajo: si el codigo no cambio, cada proyecto compila
    # en segundos en vez de desde cero.
    $buildArgs = if ($Clean) { @("clean", "build", "-x", "test") } else { @("build", "-x", "test") }

    $jobs = foreach ($service in $services) {
        Start-Job -Name $service -ScriptBlock {
            param($servicePath, $buildArgs)
            Set-Location $servicePath
            & .\gradlew.bat @buildArgs 2>&1 | Out-String -Stream | ForEach-Object { $_ }
            [PSCustomObject]@{ Success = ($LASTEXITCODE -eq 0) }
        } -ArgumentList (Join-Path $root $service), $buildArgs
    }

    Write-Host "Esperando a que terminen los 6 builds (timeout 10 min)..." -ForegroundColor Cyan
    $jobs | Wait-Job -Timeout 600 | Out-Null

    $failed = $false
    foreach ($job in $jobs) {
        if ($job.State -eq 'Running') {
            Write-Host "ERROR: $($job.Name) no termino a tiempo (posible descarga lenta del JDK toolchain)" -ForegroundColor Red
            $failed = $true
            Stop-Job -Job $job
            Remove-Job -Job $job -Force
            continue
        }

        $output = Receive-Job -Job $job
        $result = $output | Where-Object { $_ -is [PSCustomObject] -and $_.PSObject.Properties.Name -contains 'Success' } | Select-Object -Last 1

        if ($result -and $result.Success) {
            Write-Host "$($job.Name) construido exitosamente" -ForegroundColor Green
        } else {
            Write-Host "ERROR: Fallo la construccion de $($job.Name)" -ForegroundColor Red
            $output | Where-Object { $_ -isnot [PSCustomObject] } | ForEach-Object { Write-Host "  $_" -ForegroundColor DarkGray }
            $failed = $true
        }
        Remove-Job -Job $job -Force
    }

    if ($failed) {
        Write-Host ""
        Write-Host "Una o mas builds fallaron. Abortando antes de iniciar servicios." -ForegroundColor Red
        exit 1
    }
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

    $jarPath = Join-Path $root "$serviceName\build\libs\$serviceName-0.0.1-SNAPSHOT.jar"

    if (Test-Path $jarPath) {
        $envLines = @(
            "`$env:DATABASE_URL='$($env:DATABASE_URL)'",
            "`$env:DATABASE_USERNAME='$($env:DATABASE_USERNAME)'",
            "`$env:DATABASE_PASSWORD='$($env:DATABASE_PASSWORD)'",
            "`$env:JWT_SECRET='$($env:JWT_SECRET)'",
            "`$env:AZURE_ENTRA_ENABLED='$($env:AZURE_ENTRA_ENABLED)'",
            "`$env:AZURE_TENANT_ID='$($env:AZURE_TENANT_ID)'",
            "`$env:AZURE_CLIENT_ID='$($env:AZURE_CLIENT_ID)'",
            "`$env:AZURE_API_AUDIENCE='$($env:AZURE_API_AUDIENCE)'",
            "`$env:FRONTEND_URL='$($env:FRONTEND_URL)'",
            "`$env:USUARIO_SERVICE_URL='http://localhost:8081'",
            "`$env:PRODUCTO_SERVICE_URL='http://localhost:8082'",
            "`$env:CARRITO_SERVICE_URL='http://localhost:8083'",
            "`$env:VENTAS_SERVICE_URL='http://localhost:8084'",
            "`$env:BFF_SERVICE_URL='http://localhost:8085'"
        ) -join '; '

        $jarPathEscaped = $jarPath -replace "'", "''"

        Start-Process powershell -ArgumentList "-NoExit", "-Command", "
            Set-Location '$root';
            Write-Host '============================================' -ForegroundColor Cyan;
            Write-Host '  $serviceName (Puerto $port)' -ForegroundColor Cyan;
            Write-Host '============================================' -ForegroundColor Cyan;
            Write-Host '';
            Write-Host 'Swagger UI: http://localhost:$port/swagger-ui.html' -ForegroundColor Green;
            Write-Host 'API Docs: http://localhost:$port/v3/api-docs' -ForegroundColor Green;
            Write-Host '';
            $envLines;
            java -jar '$jarPathEscaped'
        "
        Write-Host "$serviceName iniciado en nueva ventana (puerto $port)" -ForegroundColor Green
    } else {
        Write-Host "ERROR: JAR no encontrado para $serviceName" -ForegroundColor Red
        Write-Host "Ruta esperada: $jarPath" -ForegroundColor Yellow
    }
}

# Espera activa a que un puerto TCP quede escuchando, en vez de un sleep fijo:
# mas rapido cuando el servicio arranca pronto, y mas confiable cuando tarda mas
# (el tiempo de arranque de Spring Boot varia bastante segun la maquina).
function Wait-ForPort {
    param (
        [int]$port,
        [int]$timeoutSeconds = 60
    )
    $deadline = (Get-Date).AddSeconds($timeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        $client = New-Object System.Net.Sockets.TcpClient
        try {
            $async = $client.BeginConnect("127.0.0.1", $port, $null, $null)
            if ($async.AsyncWaitHandle.WaitOne(500) -and $client.Connected) {
                return $true
            }
        } catch {
        } finally {
            $client.Close()
        }
        Start-Sleep -Milliseconds 500
    }
    return $false
}

# Los 4 microservicios son independientes entre si: se lanzan todos de una vez
# en lugar de uno por uno con sleeps fijos entre cada uno.
$microservicios = [ordered]@{
    "usuario-service"  = 8081
    "producto-service" = 8082
    "carrito-service"  = 8083
    "ventas-service"   = 8084
}
foreach ($svc in $microservicios.Keys) {
    Start-Service $svc $microservicios[$svc]
}

Write-Host ""
Write-Host "Esperando a que los 4 microservicios abran su puerto..." -ForegroundColor Yellow
foreach ($svc in $microservicios.Keys) {
    if (Wait-ForPort -port $microservicios[$svc]) {
        Write-Host "  $svc listo (puerto $($microservicios[$svc]))" -ForegroundColor Green
    } else {
        Write-Host "  $svc no respondio a tiempo (puerto $($microservicios[$svc])) - revisa su ventana/log" -ForegroundColor Red
    }
}

# El gateway y el BFF enrutan/orquestan hacia los 4 microservicios de arriba,
# por eso arrancan despues de que esos puertos ya estan escuchando.
Write-Host ""
Start-Service "api-gateway" 8080
Start-Service "bff" 8085

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
Write-Host "  API Gateway:      http://localhost:8080 (entrada unica, valida JWT)" -ForegroundColor Cyan
Write-Host "  BFF:              http://localhost:8085/swagger-ui.html" -ForegroundColor Cyan
Write-Host ""
Write-Host "Tip: la proxima vez, si no cambiaste codigo, usa -SkipBuild para saltar el build por completo." -ForegroundColor DarkGray
Write-Host ""
Write-Host "Presiona Ctrl+C en cada ventana para detener los servicios" -ForegroundColor Yellow
