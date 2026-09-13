# Script para instalar Gradle usando Chocolatey
# PowerShell Script

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Instalador de Gradle para Mil Sabores" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Verificar si Gradle ya esta instalado
$gradleInstalled = Get-Command gradle -ErrorAction SilentlyContinue

if ($gradleInstalled) {
    Write-Host "Gradle ya esta instalado" -ForegroundColor Green
    gradle --version
    exit 0
}

Write-Host "Gradle no esta instalado." -ForegroundColor Yellow
Write-Host ""

# Verificar si Chocolatey esta instalado
$chocoInstalled = Get-Command choco -ErrorAction SilentlyContinue

if ($chocoInstalled) {
    Write-Host "Usando Chocolatey para instalar Gradle..." -ForegroundColor Cyan
    choco install gradle -y
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "Gradle instalado exitosamente" -ForegroundColor Green
        Write-Host "Por favor, reinicia PowerShell y ejecuta run-all-services.ps1" -ForegroundColor Yellow
        exit 0
    } else {
        Write-Host "Error al instalar Gradle con Chocolatey" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "Chocolatey no esta instalado." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "OPCION 1: Instalar Chocolatey (recomendado)" -ForegroundColor Cyan
    Write-Host "Ejecuta como Administrador:" -ForegroundColor Yellow
    Write-Host "Set-ExecutionPolicy Bypass -Scope Process -Force; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))" -ForegroundColor White
    Write-Host ""
    Write-Host "Luego ejecuta: choco install gradle -y" -ForegroundColor White
    Write-Host ""
    Write-Host "OPCION 2: Descargar Gradle manualmente" -ForegroundColor Cyan
    Write-Host "1. Ve a: https://gradle.org/releases/" -ForegroundColor Yellow
    Write-Host "2. Descarga Gradle 8.11.1 (binary-only)" -ForegroundColor Yellow
    Write-Host "3. Extrae en C:\Gradle" -ForegroundColor Yellow
    Write-Host "4. Agrega C:\Gradle\bin al PATH del sistema" -ForegroundColor Yellow
    exit 1
}
