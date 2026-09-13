#!/bin/bash

# Script para iniciar todos los microservicios de Mil Sabores en EC2
# Autor: Alex Rosales Herrera
# Fecha: Diciembre 2025

echo "======================================"
echo "Iniciando Microservicios Mil Sabores"
echo "======================================"

# Configurar variables de entorno
export DATABASE_URL="jdbc:postgresql://ep-noisy-glade-acnt8zv8-pooler.sa-east-1.aws.neon.tech:5432/neondb?sslmode=require"
export DATABASE_USERNAME="neondb_owner"
export DATABASE_PASSWORD="npg_5CjH6VAeioaF"
export JWT_SECRET="milsabores-secret-key-2024-super-segura-para-produccion-cambiar"

# Crear directorio de logs si no existe
mkdir -p ~/logs

echo ""
echo "Deteniendo servicios existentes..."
pkill -f usuario-service
pkill -f producto-service
pkill -f carrito-service
pkill -f ventas-service
sleep 3

echo ""
echo "Iniciando usuario-service en puerto 8081..."
nohup java -Xms256m -Xmx512m -jar ~/usuario-service-0.0.1-SNAPSHOT.jar > ~/logs/usuario-service.log 2>&1 &
echo "Usuario-service iniciado (PID: $!)"
sleep 10

echo ""
echo "Iniciando producto-service en puerto 8082..."
nohup java -Xms256m -Xmx512m -jar ~/producto-service-0.0.1-SNAPSHOT.jar > ~/logs/producto-service.log 2>&1 &
echo "Producto-service iniciado (PID: $!)"
sleep 10

echo ""
echo "Iniciando carrito-service en puerto 8083..."
nohup java -Xms256m -Xmx512m -jar ~/carrito-service-0.0.1-SNAPSHOT.jar > ~/logs/carrito-service.log 2>&1 &
echo "Carrito-service iniciado (PID: $!)"
sleep 10

echo ""
echo "Iniciando ventas-service en puerto 8084..."
nohup java -Xms256m -Xmx512m -jar ~/ventas-service-0.0.1-SNAPSHOT.jar > ~/logs/ventas-service.log 2>&1 &
echo "Ventas-service iniciado (PID: $!)"
sleep 5

echo ""
echo "======================================"
echo "Verificando servicios..."
echo "======================================"
ps aux | grep java | grep -v grep

echo ""
echo "======================================"
echo "Verificando puertos..."
echo "======================================"
sudo ss -tulpn | grep -E ':(8081|8082|8083|8084)'

echo ""
echo "======================================"
echo "Servicios iniciados exitosamente"
echo "======================================"
echo ""
echo "URLs disponibles:"
echo "  - Usuario:  http://100.30.4.167:8081/api/usuarios"
echo "  - Producto: http://100.30.4.167:8082/api/productos"
echo "  - Carrito:  http://100.30.4.167:8083/api/carritos"
echo "  - Ventas:   http://100.30.4.167:8084/api/ventas"
echo ""
echo "Logs disponibles en ~/logs/"
echo ""
echo "Para ver logs en tiempo real:"
echo "  tail -f ~/logs/usuario-service.log"
echo "  tail -f ~/logs/producto-service.log"
echo "  tail -f ~/logs/carrito-service.log"
echo "  tail -f ~/logs/ventas-service.log"
echo ""
