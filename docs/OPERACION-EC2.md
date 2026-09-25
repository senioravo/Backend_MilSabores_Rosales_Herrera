# Operación diaria: reiniciar el lab y actualizar el código

Guía rápida para el día a día del despliegue en AWS Academy. La instalación desde cero está en [DEPLOY.md, sección 4](DEPLOY.md#4-aws-ec2-aws-academy-docker-composeec2yml).

**Datos del despliegue actual**

| Qué | Valor |
|-----|-------|
| Instancia | EC2 t3.medium, Ubuntu, en el Learner Lab (`us-east-1`) |
| IP elástica | `184.195.13.200` |
| Backend (api-gateway) | `http://184.195.13.200:8080` |
| Llave SSH | `C:\Users\diego\Downloads\MilSabores.pem` (no `labsuser.pem`) |
| Frontend | `https://dsy-1104-rosales-herrera.vercel.app` |
| Variable en Vercel | `BACKEND_URL = http://184.195.13.200:8080` |

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
1. En el Learner Lab, presionar **Start Lab** y esperar el indicador verde.
2. Abrir **AWS → EC2 → Instances** y confirmar que la instancia está en **Running**. El Learner Lab vuelve a encender las instancias que estaban corriendo; si aparece **Stopped**, seleccionarla y hacer **Instance state → Start instance**.
3. Esperar **2–3 minutos**. No hay que conectarse por SSH ni ejecutar nada.
4. Verificar:
   ```bash
   curl http://184.195.13.200:8080/actuator/health
   # {"status":"UP",...}
   ```

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
3. Esperar el mensaje `Listo`. El script compila las 5 imágenes de a una, las sube (~315 MB), reemplaza los contenedores y espera a que el gateway quede *healthy*. En total tarda unos 10–15 minutos, según la conexión.

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
free -h                                                              # memoria y swap
```
