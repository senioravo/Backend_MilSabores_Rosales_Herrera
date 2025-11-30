# Script para iniciar ventas-service con configuración correcta
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Iniciando Ventas Service (Puerto 8084)" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Configurar variables de entorno
$env:DATABASE_URL = "jdbc:postgresql://ep-noisy-glade-acnt8zv8-pooler.sa-east-1.aws.neon.tech:5432/neondb?sslmode=require"
$env:DATABASE_USERNAME = "neondb_owner"
$env:DATABASE_PASSWORD = "npg_5CjH6VAeioaF"
$env:JWT_SECRET = "milsabores-secret-key-2024-super-segura-para-produccion-cambiar"

Write-Host "Variables de entorno configuradas:" -ForegroundColor Green
Write-Host "  DATABASE_URL: $env:DATABASE_URL" -ForegroundColor Yellow
Write-Host "  DATABASE_USERNAME: $env:DATABASE_USERNAME" -ForegroundColor Yellow
Write-Host ""

Write-Host "Swagger UI estará disponible en: http://localhost:8084/swagger-ui.html" -ForegroundColor Green
Write-Host "API Docs: http://localhost:8084/v3/api-docs" -ForegroundColor Green
Write-Host ""
Write-Host "Presiona Ctrl+C para detener el servicio" -ForegroundColor Yellow
Write-Host ""

# Ejecutar el JAR
java -jar "ventas-service\build\libs\ventas-service-0.0.1-SNAPSHOT.jar"
