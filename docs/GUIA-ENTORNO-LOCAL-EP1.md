# Guía — Entorno local EP1 (MSAL + Gateway + Neon)

Rama backend recomendada: **`feature/entra-neon-identidad`**.

## Qué acabamos de dejar listo en el repo

| Cambio | Para qué |
|--------|----------|
| `run-all-services.ps1` en la **raíz** del backend | Arranca los 6 servicios desde la carpeta correcta |
| `docs/run-all-services.ps1` corregido | Lee `.env` de la raíz, JARs en `usuario-service/build/...`, pasa **AZURE_***, **JWT**, **FRONTEND_URL** |
| Front `api.config.js` | Sin `.env.local`, en dev usa **`http://localhost:8080/api`** (ya no EC2 caído) |
| `.env.example` (front y back) | Plantilla alineada con Entra + gateway |

## Lo que TÚ debes hacer (una sola vez)

### A — Backend `.env`

Desde la raíz del backend:

```powershell
cd Backend_MilSabores_Rosales_Herrera-main\Backend_MilSabores_Rosales_Herrera-main
copy .env.example .env
# Editar .env con bloc de notas: DATABASE_* de Neon, JWT_SECRET, AZURE_* reales
```

Cuando vayas a probar **login Microsoft + carrito**, en `.env` del backend:

```env
AZURE_ENTRA_ENABLED=true
```

(mismos `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `AZURE_API_AUDIENCE` que el token MSAL).

### B — Frontend `.env.local`

```powershell
cd DSY1104_ROSALES_HERRERA
copy .env.example .env.local
# Completar VITE_AZURE_* y VITE_AZURE_API_SCOPE
```

Reiniciar Vite: `npm run dev` → **http://localhost:5173**

### C — JDK

Gradle usa toolchain **Java 17**. Si falla el build, en PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot"   # tu ruta JDK 17
```

### D — Datos en Neon (recomendado)

Ejecutar en Neon el script `database/schema.sql` (productos `TC001`, etc.) o verificar que ya existen filas en `productos`.

## Arranque cada vez que demostrarás

**Terminal 1 — backend (6 ventanas):**

```powershell
cd Backend_MilSabores_Rosales_Herrera-main\Backend_MilSabores_Rosales_Herrera-main
.\run-all-services.ps1
# Segunda vez sin compilar: .\run-all-services.ps1 -SkipBuild
```

Esperar mensajes “listo” en puertos **8081–8084**, luego gateway **8080** y BFF **8085**.

**Terminal 2 — frontend:**

```powershell
cd DSY1104_ROSALES_HERRERA
npm run dev
```

## Comprobación rápida (2 minutos)

| Paso | URL / acción | Esperado |
|------|----------------|----------|
| 1 | http://localhost:8080/actuator/health | `UP` |
| 2 | http://localhost:8085/actuator/health | `UP` |
| 3 | http://localhost:8082/swagger-ui.html → GET productos | 200 + JSON |
| 4 | Front: login Microsoft | Sin error de configuración |
| 5 | DevTools → `GET http://localhost:8080/api/usuarios/me` | **200** + `{ "id": number, ... }` |

Si el paso 5 da **401**: revisar `AZURE_ENTRA_ENABLED=true`, audience/scope en Azure y consent del scope `access_as_user`.

## Puertos

| Servicio | Puerto |
|----------|--------|
| API Gateway | 8080 |
| Usuario | 8081 |
| Producto | 8082 |
| Carrito | 8083 |
| Ventas | 8084 |
| BFF | 8085 |
| Frontend Vite | 5173 |

## Documentos relacionados

- [GUIA-VALIDACION-JWT-GATEWAY.md](./GUIA-VALIDACION-JWT-GATEWAY.md)
- [GUIA-BFF-SPRING-SECURITY.md](./GUIA-BFF-SPRING-SECURITY.md)
- [IDENTIDAD-ENTRA-Y-NEON.md](./IDENTIDAD-ENTRA-Y-NEON.md)
