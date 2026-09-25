# Despliegue con contenedores (Docker, Kubernetes, Render y AWS EC2)

Cada microservicio tiene su propio `Dockerfile` en su carpeta. Todos siguen el mismo esquema:

- **Etapa de build**: `gradle:8.11.1-jdk17` compila el `bootJar` (sin tests).
- **Etapa de runtime**: `eclipse-temurin:17-jre-alpine`, usuario sin privilegios (UID 1001).
- El puerto se toma de la variable `PORT`, que por defecto es el puerto propio del servicio. Render la sobrescribe.
- Health check: `GET /actuator/health`. En Kubernetes se usan `/actuator/health/liveness` y `/actuator/health/readiness`.

| Servicio | Puerto | Carpeta |
|----------|--------|---------|
| api-gateway | 8080 | `api-gateway/` |
| usuario-service | 8081 | `usuario-service/` |
| producto-service | 8082 | `producto-service/` |
| carrito-service | 8083 | `carrito-service/` |
| ventas-service | 8084 | `ventas-service/` |
| bff | 8085 | `bff/` |

## Variables de entorno

| Variable | Usada por | Descripción |
|----------|-----------|-------------|
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | usuario, producto, carrito, ventas | Conexión a Neon PostgreSQL |
| `JWT_SECRET` | usuario-service (firma), api-gateway (valida) | Debe ser **el mismo** en ambos y tener **al menos 32 caracteres** (HS256 exige 256 bits). Generar con `openssl rand -hex 32` |
| `USUARIO_SERVICE_URL`, `PRODUCTO_SERVICE_URL`, `CARRITO_SERVICE_URL`, `VENTAS_SERVICE_URL` | api-gateway, bff | URLs de los microservicios |
| `BFF_SERVICE_URL` | api-gateway | URL del BFF |
| `FRONTEND_URL` | api-gateway | Origen permitido por CORS (frontend en Vercel) |
| `PORT` | todos | Puerto HTTP (opcional) |
| `JAVA_OPTS` | todos | Flags de la JVM (opcional) |

Las credenciales se leen siempre de `.env` o de variables de entorno. Nunca se copian a la imagen: `.dockerignore` excluye `.env`.

## 1. Docker Compose (local)

```bash
cp .env.example .env        # completar con las credenciales de Neon
docker compose up --build -d
docker compose ps           # esperar a que todos estén "healthy"
curl http://localhost:8080/actuator/health
docker compose down
```

Para construir una sola imagen:

```bash
docker build -t milsabores/usuario-service ./usuario-service
```

## 2. Kubernetes (`k8s/`)

| Archivo | Contenido |
|---------|-----------|
| `namespace.yaml` | Namespace `milsabores` |
| `configmap.yaml` | URLs internas entre servicios, `FRONTEND_URL` |
| `secret.example.yaml` | Plantilla del Secret `milsabores-secrets` |
| `<servicio>.yaml` | Deployment y Service (ClusterIP) de cada microservicio |
| `ingress.yaml` | Ingress NGINX hacia el api-gateway (`milsabores.local`) |
| `kustomization.yaml` | Aplica todo junto y permite cambiar registry y tag de las imágenes |

### Cluster local (Docker Desktop o minikube)

```bash
# 1. Construir las imágenes. En minikube, primero ejecutar: eval $(minikube docker-env)
docker compose build

# 2. Namespace y Secret (lee el .env local, no deja credenciales en el repo)
kubectl apply -f k8s/namespace.yaml
kubectl create secret generic milsabores-secrets -n milsabores --from-env-file=.env

# 3. Desplegar todo
kubectl apply -k k8s/
kubectl get pods -n milsabores -w

# 4. Acceso
kubectl port-forward -n milsabores svc/api-gateway 8080:8080
#   o por el Ingress: http://milsabores.local (agregar el host al archivo hosts)
```

### Cluster en la nube (EKS, GKE, AKS)

1. Publicar las imágenes en un registry (Docker Hub, GHCR o ECR):
   ```bash
   docker tag milsabores/usuario-service <usuario>/milsabores-usuario-service:1.0.0
   docker push <usuario>/milsabores-usuario-service:1.0.0
   ```
2. En `k8s/kustomization.yaml`, agregar `newName` y `newTag` a cada imagen.
3. Seguir los pasos 2 y 3 de la sección anterior.

> El api-gateway tiene que quedar con **1 réplica**, porque el rate limiting se guarda en memoria. Los otros servicios pueden escalarse con `kubectl scale`, pero cada réplica abre su propio pool de conexiones a Neon.

## 3. Render (`render.yaml`)

El archivo `render.yaml` de la raíz es un **Blueprint**: crea los 6 servicios como Web Services Docker.

1. Subir el repositorio a GitHub.
2. En Render, ir a **New → Blueprint** y seleccionar el repositorio.
3. Render pide `DATABASE_URL`, `DATABASE_USERNAME` y `DATABASE_PASSWORD` para cada servicio de datos. Usar los mismos valores de Neon en los cuatro.
4. `JWT_SECRET` se genera automáticamente en el grupo `milsabores-shared`, así que usuario-service y api-gateway comparten el mismo valor.
5. Revisar las URLs asignadas. Si Render agregó un sufijo a algún nombre (por ejemplo `milsabores-bff-x1y2.onrender.com`), corregir las variables `*_SERVICE_URL` del gateway y del BFF en el Dashboard.
6. En Vercel, apuntar el frontend al gateway. Los proxies de `api/` usan `https://milsabores-api-gateway.onrender.com` por defecto; la variable `BACKEND_URL` lo sobrescribe.

Notas sobre el plan free:
- Los servicios se duermen tras 15 minutos sin tráfico. El primer request puede tardar alrededor de un minuto.
- Un servicio free no recibe tráfico por la red privada de Render. Por eso gateway y BFF llaman a los demás servicios por su URL pública. En un plan pago se pueden convertir los 4 microservicios en *Private Services* (`type: pserv`) y usar URLs internas.
- Cada instancia tiene 512 MB de RAM. `JAVA_OPTS` limita la heap al 70 % de esa memoria.
- Transbank `return.url` (ventas-service) sigue apuntando al frontend en Vercel. Si cambia el dominio, actualizar `transbank.return.url`.

## 4. AWS EC2 (AWS Academy, `docker-compose.ec2.yml`)

Se usa una **t3.medium** (4 GB de RAM, 2 vCPU con créditos). `docker-compose.ec2.yml` cambia cuatro cosas respecto al compose local:

| Cambio | Motivo |
|--------|--------|
| Sin bff | El frontend no lo usa; quedan 5 JVMs en vez de 6 |
| `JAVA_OPTS` con heap de 256 MB y metaspace de 192 MB | Cada servicio queda en ~450 MB, ~2,3 GB en total; el resto queda para el sistema y Docker |
| Los 4 servicios de datos arrancan en paralelo; el gateway espera a que estén *healthy* | El arranque completo tarda ~3–5 min |
| Solo el gateway publica su puerto (8080) | Los servicios quedan en la red interna de Docker; todo pasa por el gateway, que valida el JWT |

Las imágenes **se compilan en el PC** y se copian a la instancia. Así la EC2 no gasta créditos de CPU ni memoria en Gradle.

**Si solo hay t3.small (2 GB):** también funciona, con memoria más ajustada. Agregar al `.env` antes de desplegar:

```
EC2_JAVA_OPTS=-Xms64m -Xmx160m -XX:MaxMetaspaceSize=140m -XX:ReservedCodeCacheSize=40m -Xss512k -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError
```

`ec2-setup.sh` crea 4 GB de swap automáticamente en instancias de 2 GB.

```
Navegador → Vercel (/api/* proxy) → http://<IP-elástica>:8080 (api-gateway en EC2)
                                        └→ usuario / producto / carrito / ventas (red Docker) → Neon
```

### Fase 1: infraestructura (Learner Lab)

1. **Start Lab**, esperar el indicador verde y abrir **AWS**. Región `us-east-1`.
2. Lanzar una instancia EC2:
   - AMI: **Ubuntu Server 24.04 LTS** (recomendada). Amazon Linux 2023 también sirve, pero el usuario SSH es `ec2-user`: usar `-User ec2-user` en el script.
   - Tipo: **t3.medium**.
   - Key pair: **vockey**. Descargar `labsuser.pem` desde **AWS Details**.
   - Disco: 20 GB gp3.
3. Security Group `milsabores-sg`:

   | Puerto | Origen | Motivo |
   |--------|--------|--------|
   | 22 | Mi IP | SSH |
   | 8080 | 0.0.0.0/0 | api-gateway (lo llaman las funciones de Vercel) |

4. Asignar una **IP elástica** y asociarla a la instancia. La instancia se detiene cuando termina la sesión del lab; sin IP elástica, la IP pública cambia en cada arranque y habría que actualizar Vercel.

### Fase 2: preparar la instancia (automática)

`deploy-ec2.ps1` sube y ejecuta `scripts/ec2/ec2-setup.sh` en cada despliegue, antes de compilar. El script es idempotente: instala Docker (en Ubuntu o Amazon Linux 2023), lo habilita al arranque, agrega el usuario SSH al grupo `docker` y crea swap (2 GB en una t3.medium, 4 GB en una de 2 GB), solo si falta algo. Si todo ya está listo, termina en segundos.

Alternativa: pegar el contenido de `ec2-setup.sh` en **Advanced details → User data** al lanzar la instancia, para que quede preparada desde el primer arranque.

Equivalente manual en Ubuntu, por si hace falta:

```bash
ssh -i labsuser.pem ubuntu@<IP-elástica>

# Docker oficial (incluye compose y buildx) y arranque automático
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
sudo systemctl enable docker

# Swap de 2 GB (4G en una instancia de 2 GB de RAM)
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile
sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

exit   # volver a entrar para que aplique el grupo docker
```

### Fase 3: compilar y subir (`scripts/ec2/deploy-ec2.ps1`, en el PC)

Requisitos: Rancher Desktop o Docker Desktop corriendo, `.env` en la raíz del backend y la instancia de la fase 1 con el puerto 22 abierto.

```powershell
.\scripts\ec2\deploy-ec2.ps1 -Ip <IP-elástica> -Key C:\ruta\labsuser.pem
```

El script:
1. Ajusta los permisos de `labsuser.pem` (OpenSSH en Windows rechaza llaves con permisos abiertos), prueba la conexión SSH y ejecuta la fase 2.
2. Compila las 5 imágenes con `docker-compose.ec2.yml`.
3. Las exporta a `milsabores-images.tar`.
4. Sube a `~/milsabores` en la EC2: las imágenes, `docker-compose.ec2.yml`, `.env` y `ec2-up.sh`.
5. Borra el `.tar` local.

Opciones:
- `-Up`: ejecuta también la fase 4 por SSH.
- `-SkipBuild`: reutiliza las imágenes ya compiladas.
- `-User` y `-RemoteDir`: por defecto `ubuntu` y `milsabores`.

El PC y la t3 son x86_64, así que las imágenes compiladas en Windows funcionan en la instancia.

### Fase 4: levantar (`scripts/ec2/ec2-up.sh`, en la EC2)

```bash
ssh -i labsuser.pem ubuntu@<IP-elástica> 'bash ~/milsabores/ec2-up.sh'
```

O todo junto desde el PC: `.\scripts\ec2\deploy-ec2.ps1 -Ip <IP-elástica> -Key ... -Up`.

El script:
1. Verifica Docker, `.env` y el swap.
2. Si hay un `milsabores-images.tar`, lo carga con `docker load` y lo borra.
3. Ejecuta `docker compose -f docker-compose.ec2.yml up -d`.
4. Espera hasta 15 minutos a que el gateway quede *healthy*, mostrando el estado de cada servicio.
5. Limpia las imágenes antiguas y muestra el health y la memoria usada.

Si el lab se reinicia, no hace falta volver a ejecutarlo: Docker arranca con la instancia y los contenedores tienen `restart: unless-stopped`.

### Fase 5: frontend

1. En Vercel, ir a **Settings → Environment Variables** y definir `BACKEND_URL = http://<IP-elástica>:8080`.
2. Hacer **Redeploy**. Las variables nuevas solo se aplican en un deploy nuevo.

No hace falta tocar CORS ni `transbank.return.url`: el navegador solo habla con Vercel, y los proxies de `api/` llaman al gateway desde el servidor.

Verificación desde el PC:

```bash
curl http://<IP-elástica>:8080/actuator/health
curl -X POST http://<IP-elástica>:8080/api/usuarios/login \
  -H "Content-Type: application/json" -d '{"email":"x@x.cl","password":"x"}'   # debe dar 401
```

### Operación y problemas frecuentes

Pasos para reiniciar el lab, actualizar el código y diagnosticar errores: [OPERACION-EC2.md](OPERACION-EC2.md).


- **El backend solo está disponible con el lab iniciado.** Antes de una demo, iniciar el lab unos 10 minutos antes para que los servicios terminen de arrancar.
- **Actualizar el código:** volver a ejecutar `deploy-ec2.ps1 ... -Up`.
- **Logs:** `docker compose -f ~/milsabores/docker-compose.ec2.yml logs -f --tail=100 <servicio>`.
- **Un servicio se reinicia solo:** revisar sus logs.
  - `OutOfMemoryError: Metaspace` → subir `-XX:MaxMetaspaceSize`.
  - `Java heap space` → subir `-Xmx`.
  - Se ajustan con `EC2_JAVA_OPTS` en el `.env` (o cambiando el valor por defecto en `docker-compose.ec2.yml`) y volviendo a ejecutar `ec2-up.sh`.
- **`Permission denied (publickey)`:** usar la llave del key pair con que se lanzó la instancia. Si al lanzarla se creó un key pair propio (por ejemplo `MilSabores.pem`), `labsuser.pem` no sirve.
- **Con Rancher Desktop, `docker` busca `dockerDesktopLinuxEngine`:** el contexto activo quedó de Docker Desktop. Ejecutar el script con `$env:DOCKER_CONTEXT = "default"` antes, o `docker context use default` una vez.
- **`timed out dialing Hyper-V socket` al compilar:** el Docker local se colgó. Reiniciar Rancher Desktop (`rdctl shutdown` y `rdctl start`) y volver a ejecutar el script; las imágenes se compilan de a una para no sobrecargarlo.
- **Arranque muy lento:** la t3 se quedó sin créditos de CPU. Se ve en la pestaña Monitoring de la instancia, en *CPU credit balance*.
