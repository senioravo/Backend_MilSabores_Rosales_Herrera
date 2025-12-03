#!/bin/bash

# Script para detener todos los microservicios de Mil Sabores en EC2
# Autor: Alex Rosales Herrera
# Fecha: Diciembre 2025

echo "======================================"
echo "Deteniendo Microservicios Mil Sabores"
echo "======================================"

echo ""
echo "Buscando procesos Java en ejecución..."
ps aux | grep java | grep -v grep

echo ""
echo "Deteniendo usuario-service..."
pkill -f usuario-service
sleep 2

echo "Deteniendo producto-service..."
pkill -f producto-service
sleep 2

echo "Deteniendo carrito-service..."
pkill -f carrito-service
sleep 2

echo "Deteniendo ventas-service..."
pkill -f ventas-service
sleep 2

echo ""
echo "======================================"
echo "Verificando procesos restantes..."
echo "======================================"
REMAINING=$(ps aux | grep java | grep -v grep)

if [ -z "$REMAINING" ]; then
    echo "✓ Todos los servicios han sido detenidos correctamente"
else
    echo "⚠ Aún hay procesos Java en ejecución:"
    echo "$REMAINING"
    echo ""
    echo "Para forzar la detención, ejecuta:"
    echo "  killall -9 java"
fi

echo ""
echo "======================================"
echo "Verificando puertos liberados..."
echo "======================================"
sudo ss -tulpn | grep -E ':(8081|8082|8083|8084)' || echo "✓ Todos los puertos liberados (8081-8084)"

echo ""
