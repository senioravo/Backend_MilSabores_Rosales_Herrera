# Operación diaria: reiniciar el lab y actualizar el código

Guía rápida para el día a día del despliegue en AWS Academy. La instalación desde cero está en [DEPLOY.md, sección 4](DEPLOY.md#4-aws-ec2-aws-academy-docker-composeec2yml).

**Datos del despliegue actual**

Son **cuatro instancias EC2** (todas en el mismo Learner Lab, `us-east-1`, misma VPC/security group): la de la app (de siempre) y, desde EP3, tres más solo para Kafka. El diagrama completo: [`docs/diagramas/topologia-aws-actual.svg`](diagramas/topologia-aws-actual.svg).

| Qué | Valor |
|-----|-------|
| Instancia app | EC2 t3.medium, Ubuntu |
| IP elástica (app) | `184.195.13.200` |
| Backend (api-gateway) | `http://184.195.13.200:8080` |
| Kafka-UI | `http://184.195.13.200:8090` (Security Group: Mi IP) |
| RabbitMQ rabbit1 | `http://184.195.13.200:15672` (usuario `admin`, clave la de `RABBITMQ_PASSWORD`) |
| RabbitMQ rabbit2 | `http://184.195.13.200:15673` |
| Llave SSH (las cuatro instancias) | `C:\Users\diego\Downloads\MilSabores.pem` (no `labsuser.pem`) |
| kafka-node-1 | EC2 t3.small · IP elástica `18.204.181.19` · IP privada `172.31.8.219` · `KAFKA_NODE_ID=1` |
| kafka-node-2 | EC2 t3.small · IP elástica `54.88.100.145` · IP privada `172.31.9.130` · `KAFKA_NODE_ID=2` |
| kafka-node-3 | EC2 t3.small · IP elástica `3.226.29.156` · IP privada `172.31.4.202` · `KAFKA_NODE_ID=3` |
| Frontend | `https://dsy-1104-rosales-herrera.vercel.app` |
| Variable en Vercel | `BACKEND_URL = http://184.195.13.200:8080` |

Las cuatro instancias (app + los tres nodos de Kafka) ya tienen IP elástica fija: no cambian aunque se detenga y vuelva a arrancar el lab. No hace falta entrar a los nodos de Kafka por IP pública para el uso normal: la app les habla por IP privada. La IP pública solo se usa para SSH si hay que revisarlos o reconfigurarlos.

Resumen:

| Situación | ¿Hay que hacer algo? |
|-----------|----------------------|
| Reiniciar el lab de AWS | Solo **Start Lab** y esperar ~3 min. El backend arranca solo |
| Frontend en Vercel | Nada. Siempre está arriba |
| Cambio de código en el backend | Ejecutar `deploy-ec2.ps1 ... -Up` |
| Cambio de código en el frontend | `git push` a `main` |
| Cambio en el `.env` del backend | Subir el `.env` y recrear los servicios (sección 3.2) |
| Cambio de variables en Vercel | Redeploy en Vercel |

---

## 1. Reiniciar el lab de AWS (EC2)

### Qué pasa al terminar la sesión
Cuando se acaba el tiempo de la sesión (o se presiona **End Lab**), AWS Academy **detiene** la instancia. No se borra nada: el disco, las imágenes Docker, el `.env` y la IP elástica se conservan.

### Pasos al volver
1. En el Learner Lab, presionar **Start Lab** y esperar el indicador verde. Esto enciende las **cuatro** instancias (la de la app y los tres nodos de Kafka), no solo una.
2. Abrir **AWS → EC2 → Instances** y confirmar que las cuatro están en **Running**. El Learner Lab vuelve a encender las que estaban corriendo; si alguna aparece **Stopped**, seleccionarla y hacer **Instance state → Start instance**.
3. Esperar **2–3 minutos**. No hay que conectarse por SSH ni ejecutar nada.
4. Verificar:
   ```bash
   curl http://184.195.13.200:8080/actuator/health
   # {"status":"UP",...}
   ```
   Si el gateway responde pero algo que depende de Kafka falla, es casi siempre que algún nodo de Kafka tardó más en levantar que la app — ver la sección 5.

### Por qué no hace falta hacer nada más
- **Docker arranca con la instancia:** `ec2-setup.sh` lo dejó habilitado con `systemctl enable docker`.
- **Los contenedores se levantan solos:** tienen `restart: unless-stopped` en `docker-compose.ec2.yml`.
- **La IP no cambia:** la IP elástica sigue asociada a la instancia aunque se detenga. Por eso Vercel no necesita cambios.
- **La base de datos está fuera de AWS:** Neon no depende del lab.

### Cuidados
- **No presionar Reset en el Learner Lab.** Reset borra todos los recursos (instancia, IP elástica, Security Group). Habría que repetir el despliegue completo de `DEPLOY.md` y actualizar `BACKEND_URL` en Vercel con la IP nueva.
- **La primera petición puede tardar unos segundos:** Neon suspende la base de datos tras un rato sin uso y la despierta con la primera consulta.
- **Antes de una demo o evaluación,** iniciar el lab unos 10 minutos antes.
- **Si el SSH deja de conectar** (`Connection timed out`): la regla del puerto 22 solo permite "My IP" y tu IP de casa cambió. Editar la regla en **EC2 → Security Groups → milsabores-sg → Edit inbound rules** y volver a elegir **My IP**. Esto no afecta al backend (puerto 8080), solo a SSH y a los despliegues.

---

## 2. Frontend (Vercel)

### Al reiniciar
**No hay que hacer nada.** Vercel no depende del lab: el sitio y las funciones proxy (`api/`) están siempre disponibles. Si el lab está apagado, el sitio carga pero el login y el carrito fallan hasta que la EC2 vuelva a estar arriba.

### Al actualizar el código del frontend
1. Hacer commit y `git push` a la rama `main` del repositorio `senioravo/DSY1104_ROSALES_HERRERA`.
2. Vercel despliega solo en producción. Revisar en **Vercel → Deployments** que el deploy quede en **Ready**.

### Al cambiar variables de entorno en Vercel
Las variables solo se aplican en un deploy nuevo:
1. **Settings → Environment Variables**, editar o crear la variable (con **Production** marcado).
2. **Deployments** → último deploy → **⋯ → Redeploy**.

Solo hace falta cambiar `BACKEND_URL` si cambia la IP del backend (por ejemplo, después de un Reset del lab).

---

## 3. Actualizar el backend

Requisitos en el PC: el lab iniciado, Rancher Desktop corriendo y la carpeta del backend abierta en PowerShell.

### 3.1 Cambios de código
1. Hacer commit de los cambios (recomendado, para saber qué versión está desplegada).
2. Ejecutar:
   ```powershell
   $env:DOCKER_CONTEXT = "default"
   .\scripts\ec2\deploy-ec2.ps1 -Ip 184.195.13.200 -Key C:\Users\diego\Downloads\MilSabores.pem -Up
   ```
3. Esperar el mensaje `Listo`. El script compila las imágenes de a una (10 desde que está Kafka), las sube (~490 MB), reemplaza los contenedores y espera a que el gateway quede *healthy*. En total tarda unos 15–20 minutos, según la conexión.

   **Si falla "no space left on device" al cargar las imágenes:** la EC2 se quedó sin disco (ver la nota de disco en `DEPLOY.md`, sección 4). Liberar espacio bajando el stack viejo y podando sus imágenes, y reintentar con `-SkipBuild` (ya están compiladas, no hace falta repetir el build):
   ```bash
   ssh -i C:\Users\diego\Downloads\MilSabores.pem ubuntu@184.195.13.200 \
     'cd ~/milsabores && sudo docker compose -f docker-compose.ec2.yml down && sudo docker image prune -a -f'
   ```
   ```powershell
   .\scripts\ec2\deploy-ec2.ps1 -Ip 184.195.13.200 -Key C:\Users\diego\Downloads\MilSabores.pem -SkipBuild -Up
   ```

El backend queda sin servicio 1–2 minutos, mientras se reemplazan los contenedores.

**Cambios en las entidades JPA:** no hace falta migrar nada a mano. Los 4 servicios usan `spring.jpa.hibernate.ddl-auto=update`, así que Hibernate agrega tablas y columnas nuevas al arrancar. No borra ni renombra columnas: eso sí hay que hacerlo en Neon.

### 3.2 Cambios solo en el `.env` (credenciales de Neon, `JWT_SECRET`)
No hace falta recompilar. En Git Bash, desde la carpeta del backend:
```bash
scp -i /c/Users/diego/Downloads/MilSabores.pem .env ubuntu@184.195.13.200:~/milsabores/.env
ssh -i /c/Users/diego/Downloads/MilSabores.pem ubuntu@184.195.13.200 \
  'cd ~/milsabores && docker compose -f docker-compose.ec2.yml up -d --force-recreate'
```
`JWT_SECRET` debe tener **al menos 32 caracteres**; si es más corto, el login falla con "key byte array ... not secure enough". Al cambiarlo, los usuarios deben volver a iniciar sesión.

### 3.3 Reiniciar los servicios sin cambios
Por ejemplo, si uno quedó en mal estado:
```bash
ssh -i /c/Users/diego/Downloads/MilSabores.pem ubuntu@184.195.13.200 'bash ~/milsabores/ec2-up.sh'
```
`ec2-up.sh` vuelve a levantar lo que falte y espera a que todo quede *healthy*. Para reiniciar un solo servicio:
```bash
ssh -i /c/Users/diego/Downloads/MilSabores.pem ubuntu@184.195.13.200 \
  'cd ~/milsabores && docker compose -f docker-compose.ec2.yml restart usuario-service'
```

---

## 4. Diagnóstico rápido

| Síntoma en el frontend | Causa probable | Qué hacer |
|------------------------|----------------|-----------|
| Login da **500** con `Proxy error` | La EC2 está apagada o el lab no está iniciado | Sección 1 |
| Login da **503** con "Service Suspended" | Vercel sigue apuntando a Render | Revisar `BACKEND_URL` en Vercel y hacer Redeploy |
| Login da 500 con "not secure enough" | `JWT_SECRET` de menos de 32 caracteres | Sección 3.2 |
| Carrito o compra dan **401** | Sesión sin token o vencida (duran 24 h) | Cerrar sesión y volver a entrar |
| Todo falla justo después de Start Lab | Los servicios siguen arrancando | Esperar 2–3 min y reintentar |
| La primera petición tarda mucho y luego funciona | Neon estaba suspendido | Nada; es normal |

Comandos útiles (por SSH):
```bash
ssh -i /c/Users/diego/Downloads/MilSabores.pem ubuntu@184.195.13.200
cd ~/milsabores
docker compose -f docker-compose.ec2.yml ps                          # estado de los servicios
docker compose -f docker-compose.ec2.yml logs --tail=100 usuario-service
docker compose -f docker-compose.ec2.yml exec rabbit1 rabbitmqctl cluster_status
free -h                                                              # memoria y swap
```

La web del clúster es `http://184.195.13.200:15672` y `:15673`. Si no abre, en el Security Group los puertos 15672 y 15673 tienen que permitir **My IP**, igual que el 22. El usuario es `admin`.

---

## 5. Kafka: operación de los tres nodos

Los tres nodos (`kafka-node-1/2/3`) no forman parte de `docker-compose.ec2.yml`: viven en sus propias EC2, con `docker-compose.kafka-node.yml`. Se reinician solos con **Start Lab** (`restart: unless-stopped`), igual que la EC2 de la app.

### Revisar el clúster

```bash
for ip in 18.204.181.19 54.88.100.145 3.226.29.156; do
  echo "=== $ip ==="
  ssh -i /c/Users/diego/Downloads/MilSabores.pem ubuntu@$ip "sudo docker ps --format 'table {{.Names}}\t{{.Status}}'"
done
```

`zookeeper` y `kafka` tienen que estar *healthy* en los tres. Si uno se queda mucho rato en *starting*, revisar que los otros dos también estén arriba: Zookeeper no arranca solo, necesita quórum (al menos 2 de 3).

### Un tópico no aparece o `kafka-admin` no responde

```bash
ssh -i /c/Users/diego/Downloads/MilSabores.pem ubuntu@184.195.13.200 \
  'cd ~/milsabores && sudo docker compose -f docker-compose.ec2.yml exec kafka-admin wget -qO- http://localhost:8087/api/kafka-admin/topics'
```

Si da error de conexión, el problema está entre la EC2 de la app y los nodos (revisar `KAFKA_BOOTSTRAP_SERVERS` en el `.env` y las reglas 9092 del security group), no en `kafka-admin` mismo.

### Reiniciar un nodo

```bash
ssh -i /c/Users/diego/Downloads/MilSabores.pem ubuntu@<IP-pública-del-nodo> \
  'cd ~/milsabores-kafka && sudo docker compose -f docker-compose.kafka-node.yml restart'
```

Los otros dos siguen sirviendo mientras tanto (por eso están separados).

### Cambiar el código de Kafka (kafka-admin, kafka-topology, o los publishers/listeners de producto/ventas)

Los tres nodos **no se tocan**: ese código corre en la EC2 de la app. Alcanza con el despliegue normal (sección 3.1).

### Las instancias de Kafka no aparecen al hacer `docker ps`

Los tres nodos ya tienen IP elástica fija (ver tabla arriba), así que la IP pública no debería cambiar. Si aun así no responde por SSH, confirmar en **EC2 → Instances** que la IP elástica sigue asociada a la instancia correcta (se puede desasociar si alguien relanza la instancia desde cero). La IP **privada** (la que de verdad importa, la que está en `KAFKA_BOOTSTRAP_SERVERS`) no cambia mientras no se borre la instancia.
