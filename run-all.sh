#!/bin/bash
# Script para ejecutar todos los microservicios en modo desarrollo
# Ejecutar desde la carpeta BackendMilSabores

echo "🚀 Iniciando microservicios de Mil Sabores..."

# Función para ejecutar un servicio en background
run_service() {
    local service_name=$1
    local port=$2
    
    echo "▶️  Iniciando $service_name en puerto $port..."
    cd $service_name
    ./gradlew bootRun > "../logs/$service_name.log" 2>&1 &
    echo $! > "../logs/$service_name.pid"
    cd ..
    echo "✓ $service_name iniciado (PID: $(cat logs/$service_name.pid))"
}

# Crear directorio de logs si no existe
mkdir -p logs

# Iniciar servicios
run_service "usuario-service" 8081
sleep 5
run_service "producto-service" 8082
sleep 5
run_service "carrito-service" 8083

echo ""
echo "✨ Todos los servicios están iniciando..."
echo ""
echo "📊 URLs de los servicios:"
echo "   - Usuario Service:  http://localhost:8081/swagger-ui.html"
echo "   - Producto Service: http://localhost:8082/swagger-ui.html"
echo "   - Carrito Service:  http://localhost:8083/swagger-ui.html"
echo ""
echo "📝 Logs disponibles en:"
echo "   - logs/usuario-service.log"
echo "   - logs/producto-service.log"
echo "   - logs/carrito-service.log"
echo ""
echo "⚠️  Para detener los servicios, ejecuta: ./stop-all.sh"
