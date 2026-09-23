# Despliegue con contenedores (Docker, Kubernetes y Render)

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
| `JWT_SECRET` | usuario-service (firma), api-gateway (valida) | Debe ser **el mismo** en ambos |
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
6. En Vercel, apuntar el frontend a `https://milsabores-api-gateway.onrender.com`.

Notas sobre el plan free:
- Los servicios se duermen tras 15 minutos sin tráfico. El primer request puede tardar alrededor de un minuto.
- Un servicio free no recibe tráfico por la red privada de Render. Por eso gateway y BFF llaman a los demás servicios por su URL pública. En un plan pago se pueden convertir los 4 microservicios en *Private Services* (`type: pserv`) y usar URLs internas.
- Cada instancia tiene 512 MB de RAM. `JAVA_OPTS` limita la heap al 70 % de esa memoria.
- Transbank `return.url` (ventas-service) sigue apuntando al frontend en Vercel. Si cambia el dominio, actualizar `transbank.return.url`.
