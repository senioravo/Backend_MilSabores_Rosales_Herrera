#!/bin/bash
# Script para construir todos los microservicios
# Ejecutar desde la carpeta BackendMilSabores

echo "🔨 Construyendo microservicios de Mil Sabores..."

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Usuario Service
echo "📦 Construyendo Usuario Service..."
cd usuario-service
if ./gradlew clean build; then
    echo -e "${GREEN}✓ Usuario Service construido exitosamente${NC}"
else
    echo -e "${RED}✗ Error al construir Usuario Service${NC}"
    exit 1
fi
cd ..

# Producto Service
echo "📦 Construyendo Producto Service..."
cd producto-service
if ./gradlew clean build; then
    echo -e "${GREEN}✓ Producto Service construido exitosamente${NC}"
else
    echo -e "${RED}✗ Error al construir Producto Service${NC}"
    exit 1
fi
cd ..

# Carrito Service
echo "📦 Construyendo Carrito Service..."
cd carrito-service
if ./gradlew clean build; then
    echo -e "${GREEN}✓ Carrito Service construido exitosamente${NC}"
else
    echo -e "${RED}✗ Error al construir Carrito Service${NC}"
    exit 1
fi
cd ..

echo ""
echo -e "${GREEN}✨ Todos los microservicios construidos exitosamente!${NC}"
echo ""
echo "📂 JARs generados en:"
echo "   - usuario-service/build/libs/usuario-service-0.0.1-SNAPSHOT.jar"
echo "   - producto-service/build/libs/producto-service-0.0.1-SNAPSHOT.jar"
echo "   - carrito-service/build/libs/carrito-service-0.0.1-SNAPSHOT.jar"
