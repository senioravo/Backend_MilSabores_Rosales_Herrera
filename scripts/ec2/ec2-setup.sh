#!/usr/bin/env bash
# ============================================================
#  Fase 2 - prepara una EC2 (Ubuntu o Amazon Linux 2023) para el backend (idempotente)
#
#  - Instala Docker (con compose y buildx) si no está.
#  - Habilita Docker al arranque y agrega el usuario al grupo docker.
#  - Crea swap (4 GB con <= 2 GB de RAM, si no 2 GB) si la instancia no tiene.
#
#  deploy-ec2.ps1 lo ejecuta en cada despliegue; si todo ya está
#  listo, termina en segundos. También sirve como "User data" al
#  lanzar la instancia (ahí corre como root).
# ============================================================
set -euo pipefail

SWAP_FILE=/swapfile
# 4 GB de swap en instancias de hasta 2 GB de RAM (t3.small), 2 GB en las demás
RAM_MB=$(awk '/MemTotal/ {print int($2 / 1024)}' /proc/meminfo)
if [ "$RAM_MB" -lt 3000 ]; then SWAP_SIZE=4G; else SWAP_SIZE=2G; fi

# Usuario por defecto de la AMI: ubuntu (Ubuntu) o ec2-user (Amazon Linux)
TARGET_USER="${SUDO_USER:-${USER:-root}}"
if [ "$TARGET_USER" = "root" ]; then
    if id ubuntu > /dev/null 2>&1; then TARGET_USER=ubuntu; else TARGET_USER=ec2-user; fi
fi

SUDO=""
[ "$(id -u)" -ne 0 ] && SUDO="sudo"

. /etc/os-release

if ! command -v docker > /dev/null; then
    echo "==> Instalando Docker ($PRETTY_NAME)"
    if [ "$ID" = "amzn" ]; then
        # Amazon Linux 2023: Docker viene en los repos, el plugin compose no
        $SUDO dnf install -y docker
        COMPOSE_DIR=/usr/local/lib/docker/cli-plugins
        $SUDO mkdir -p "$COMPOSE_DIR"
        $SUDO curl -fsSL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-$(uname -m)" -o "$COMPOSE_DIR/docker-compose"
        $SUDO chmod +x "$COMPOSE_DIR/docker-compose"
    else
        curl -fsSL https://get.docker.com | $SUDO sh
    fi
else
    echo "==> Docker ya instalado: $(docker --version)"
fi

$SUDO systemctl enable --now docker > /dev/null 2>&1

if ! id -nG "$TARGET_USER" | grep -qw docker; then
    echo "==> Agregando $TARGET_USER al grupo docker (aplica en la próxima conexión SSH)"
    $SUDO usermod -aG docker "$TARGET_USER"
fi

if ! swapon --show | grep -q .; then
    echo "==> Creando swap de $SWAP_SIZE"
    $SUDO fallocate -l "$SWAP_SIZE" "$SWAP_FILE"
    $SUDO chmod 600 "$SWAP_FILE"
    $SUDO mkswap "$SWAP_FILE" > /dev/null
    $SUDO swapon "$SWAP_FILE"
    grep -q "^$SWAP_FILE " /etc/fstab || echo "$SWAP_FILE none swap sw 0 0" | $SUDO tee -a /etc/fstab > /dev/null
else
    echo "==> Swap ya configurado"
fi

free -h
