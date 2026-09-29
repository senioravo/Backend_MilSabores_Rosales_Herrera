# ============================================================
#  Fases 2 y 3 - prepara la EC2 (ec2-setup.sh), compila las imágenes
#  en este PC y las sube; con -Up también ejecuta la fase 4
#
#  Uso (desde cualquier carpeta):
#    .\scripts\ec2\deploy-ec2.ps1 -Ip <IP-elástica> -Key C:\ruta\labsuser.pem
#    .\scripts\ec2\deploy-ec2.ps1 -Ip <IP-elástica> -Key ... -Up          # además ejecuta la fase 4
#    .\scripts\ec2\deploy-ec2.ps1 -Ip <IP-elástica> -Key ... -SkipBuild   # reutiliza las imágenes ya compiladas
#
#  Requisitos: Docker (Rancher/Docker Desktop) corriendo, .env en la raíz
#  del backend y una EC2 Ubuntu con el puerto 22 abierto (fase 1).
# ============================================================
param(
    [Parameter(Mandatory = $true)][string]$Ip,
    [Parameter(Mandatory = $true)][string]$Key,
    [string]$User = "ubuntu",
    [string]$RemoteDir = "milsabores",
    [switch]$SkipBuild,
    [switch]$Up
)

$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$ComposeFile = "docker-compose.ec2.yml"
$ImagesTar = "milsabores-images.tar"
$Images = @(
    "milsabores/usuario-service:latest",
    "milsabores/producto-service:latest",
    "milsabores/carrito-service:latest",
    "milsabores/ventas-service:latest",
    "milsabores/api-gateway:latest"
)
$Target = "$User@$Ip"
$SshOpts = @("-i", $Key, "-o", "StrictHostKeyChecking=accept-new")

function Invoke-Step([string]$Description, [scriptblock]$Command) {
    Write-Host "==> $Description" -ForegroundColor Cyan
    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "Falló: $Description (código $LASTEXITCODE)"
    }
}

Push-Location $Root
try {
    # ---------- Verificaciones ----------
    if (-not (Test-Path ".env")) { throw "Falta .env en $Root (copiar .env.example y completarlo)." }
    if (-not (Test-Path $Key)) { throw "No existe la llave $Key (descargarla desde AWS Details en el Learner Lab)." }

    # OpenSSH en Windows rechaza llaves con permisos abiertos ("UNPROTECTED PRIVATE KEY FILE")
    icacls $Key /inheritance:r /grant:r "$($env:USERNAME):(R)" | Out-Null

    docker info *> $null
    if ($LASTEXITCODE -ne 0) { throw "Docker no está corriendo. Iniciar Rancher Desktop o Docker Desktop." }

    # ---------- Fase 2: preparar la instancia (idempotente) ----------
    # Va antes de compilar para detectar problemas de SSH sin esperar el build
    Invoke-Step "Probando conexión SSH con $Target" {
        ssh @SshOpts -o ConnectTimeout=15 $Target "mkdir -p ~/$RemoteDir"
    }
    Invoke-Step "Subiendo ec2-setup.sh" {
        scp @SshOpts "scripts/ec2/ec2-setup.sh" "${Target}:~/$RemoteDir/"
    }
    Invoke-Step "Preparando la instancia (Docker y swap)" {
        ssh @SshOpts $Target "sed -i 's/\r$//' ~/$RemoteDir/ec2-setup.sh && bash ~/$RemoteDir/ec2-setup.sh"
    }

    # ---------- Compilar y exportar ----------
    if (-not $SkipBuild) {
        # De a una: 5 builds de Gradle en paralelo pueden colgar el Docker local
        foreach ($Service in "usuario-service", "producto-service", "carrito-service", "ventas-service", "api-gateway") {
            Invoke-Step "Compilando $Service (varios minutos la primera vez)" {
                docker compose -f $ComposeFile build $Service
            }
        }
    }

    Invoke-Step "Exportando imágenes a $ImagesTar" {
        docker save -o $ImagesTar @Images
    }
    $SizeMb = [math]::Round((Get-Item $ImagesTar).Length / 1MB)
    Write-Host "    $ImagesTar pesa $SizeMb MB"

    # ---------- Subir ----------
    Invoke-Step "Subiendo imágenes, compose, .env y ec2-up.sh" {
        scp @SshOpts $ImagesTar $ComposeFile ".env" "scripts/ec2/ec2-up.sh" "${Target}:~/$RemoteDir/"
    }
    # Por si el .sh salió de Windows con fin de línea CRLF
    Invoke-Step "Ajustando fin de línea de ec2-up.sh" {
        ssh @SshOpts $Target "sed -i 's/\r$//' ~/$RemoteDir/ec2-up.sh"
    }

    Remove-Item $ImagesTar
    Write-Host "    $ImagesTar local eliminado"

    # ---------- Fase 4 ----------
    if ($Up) {
        Invoke-Step "Ejecutando fase 4 en la EC2" {
            ssh @SshOpts $Target "bash ~/$RemoteDir/ec2-up.sh"
        }
    } else {
        Write-Host ""
        Write-Host "Archivos subidos. Para levantar el backend (fase 4):" -ForegroundColor Green
        Write-Host "    ssh -i $Key $Target 'bash ~/$RemoteDir/ec2-up.sh'"
    }
}
finally {
    if (Test-Path $ImagesTar) { Remove-Item $ImagesTar }
    Pop-Location
}
