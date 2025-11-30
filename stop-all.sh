#!/bin/bash
# Script para detener todos los microservicios
# Ejecutar desde la carpeta BackendMilSabores

echo "🛑 Deteniendo microservicios de Mil Sabores..."

# Función para detener un servicio
stop_service() {
    local service_name=$1
    local pid_file="logs/$service_name.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat $pid_file)
        echo "⏹️  Deteniendo $service_name (PID: $pid)..."
        kill $pid 2>/dev/null
        rm $pid_file
        echo "✓ $service_name detenido"
    else
        echo "⚠️  No se encontró PID para $service_name"
    fi
}

# Detener servicios
stop_service "usuario-service"
stop_service "producto-service"
stop_service "carrito-service"

echo ""
echo "✨ Todos los servicios han sido detenidos"
