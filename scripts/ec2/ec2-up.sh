#!/usr/bin/env bash
# ============================================================
#  Fase 4 - levanta el backend en la EC2 (se ejecuta EN la instancia)
#
#  Espera en la misma carpeta (la sube deploy-ec2.ps1):
#    docker-compose.ec2.yml, .env y, si hay imágenes nuevas,
#    milsabores-images.tar
#
#  Uso:  bash ~/milsabores/ec2-up.sh
# ============================================================
set -euo pipefail

cd "$(dirname "$0")"

COMPOSE_FILE="docker-compose.ec2.yml"
IMAGES_TAR="milsabores-images.tar"
TIMEOUT_SECONDS=900   # en una t3.medium el arranque completo tarda ~3-5 min

compose() { docker compose -f "$COMPOSE_FILE" "$@"; }

# ---------- Verificaciones ----------
command -v docker > /dev/null || { echo "ERROR: Docker no está instalado (ver fase 2 en docs/DEPLOY.md)."; exit 1; }
docker info > /dev/null 2>&1 || { echo "ERROR: sin acceso a Docker. ¿Falta 'sudo usermod -aG docker \$USER' y volver a entrar?"; exit 1; }
[ -f "$COMPOSE_FILE" ] || { echo "ERROR: falta $COMPOSE_FILE en $(pwd)."; exit 1; }
[ -f .env ] || { echo "ERROR: falta .env en $(pwd)."; exit 1; }
chmod 600 .env

if ! swapon --show | grep -q .; then
    echo "AVISO: la instancia no tiene swap. Ejecutar ec2-setup.sh para crearlo."
fi

# ---------- Cargar imágenes nuevas ----------
if [ -f "$IMAGES_TAR" ]; then
    echo "==> Cargando imágenes desde $IMAGES_TAR"
    docker load -i "$IMAGES_TAR"
    rm -f "$IMAGES_TAR"   # libera disco; cada despliegue sube uno nuevo
fi

# ---------- Levantar ----------
echo "==> Levantando servicios"
compose up -d --remove-orphans

echo "==> Esperando a que el api-gateway quede healthy (máx. $((TIMEOUT_SECONDS / 60)) min)"
elapsed=0
while true; do
    gateway_id="$(compose ps -q api-gateway)"
    status="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$gateway_id" 2> /dev/null || echo "no creado")"
    [ "$status" = "healthy" ] && break
    if [ "$elapsed" -ge "$TIMEOUT_SECONDS" ]; then
        echo "ERROR: el gateway no quedó healthy. Estado de los servicios:"
        compose ps
        echo "Revisar logs con: docker compose -f $COMPOSE_FILE logs --tail=100 <servicio>"
        exit 1
    fi
    printf '   %3ss  %s\n' "$elapsed" "$(compose ps --format '{{.Service}}={{.Health}}' | tr '\n' ' ')"
    sleep 20
    elapsed=$((elapsed + 20))
done

# Imágenes anteriores que quedaron sin tag tras cargar las nuevas
docker image prune -f > /dev/null

echo
compose ps
echo
echo "==> Health: $(curl -s http://localhost:8080/actuator/health)"
echo
free -h
echo
echo "Listo. Backend disponible en http://<IP-elástica>:8080"
