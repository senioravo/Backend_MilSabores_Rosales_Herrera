# Script para ejecutar producto-service con variables de entorno
# PowerShell Script

# Cambiar al directorio del script
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# Cargar variables de entorno
if (Test-Path "../.env") {
    Get-Content ../.env | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
        }
    }
}

Write-Host "Iniciando producto-service..."
Write-Host "DATABASE_URL: $env:DATABASE_URL"
java -jar build\libs\producto-service-0.0.1-SNAPSHOT.jar
