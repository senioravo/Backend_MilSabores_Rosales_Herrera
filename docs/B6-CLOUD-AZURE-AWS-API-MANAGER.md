# B6 — Azure (consent / producción) y API Manager en la nube

Guía EP1 para la **presentación** y el despliegue. No reemplaza B1–B5 en código; conecta portal Azure, frontend en Vercel y **punto de entrada público** al backend.

## Mapa mental

| Entorno | “API Manager” / puerta pública | Validación JWT Entra |
|---------|--------------------------------|----------------------|
| **Local** | `http://localhost:8080` (Spring Cloud Gateway) | `JwtAuthenticationFilter` + `ReactiveJwtDecoder` |
| **Nube (proyecto)** | `https://milsabores-api-gateway.onrender.com` (Render) | Mismo gateway desplegado con `AZURE_*` |
| **Demo curso AWS** | **Amazon API Gateway** (HTTP API) delante del backend | Opción A: proxy y valida Spring en Render. Opción B: authorizer en AWS (avanzado). |

En la rúbrica, “API Manager” = **una URL pública** hacia el backend + **rechazar tokens inválidos**. En Mil Sabores la validación Entra ya está en el **api-gateway Spring**; AWS API Gateway puede ser **capa extra** de enrutamiento HTTPS/DNS para la demo.

---

## Parte 1 — Azure Portal (consent y MSAL en prod)

Hacer **una vez** por tenant (LUIS ROSALES / tu app Mil Sabores frontend).

### 1.1 Redirect URIs (SPA)

**App registrations** → tu app SPA → **Authentication** → **Single-page application**:

| URI | Uso |
|-----|-----|
| `http://localhost:5173` | Desarrollo |
| `https://<tu-proyecto>.vercel.app` | Producción (ej. `dsy-1104-rosales-herrera.vercel.app`) |

Guardar. Debe coincidir con `window.location.origin` del front.

### 1.2 Expose an API + scope

**App registrations** → **Expose an API**:

- Application ID URI: `api://<client-id>` (mismo valor que `VITE_AZURE_API_SCOPE` / `AZURE_API_AUDIENCE` sin el sufijo `/access_as_user` en audience; el scope suele ser `access_as_user`).

**Frontend** `.env.local` / Vercel:

```env
VITE_AZURE_API_SCOPE=api://<client-id>/access_as_user
```

**Backend** Render / `.env` (api-gateway):

```env
AZURE_API_AUDIENCE=api://<client-id>
```

### 1.3 Permisos API + consentimiento

**App registrations** → **API permissions** → **Add a permission** → **My APIs** → seleccionar la misma app → marcar **`access_as_user`**.

Luego **Grant admin consent for [tenant]** (botón azul). Sin esto, `acquireTokenSilent` puede fallar en producción.

### 1.4 Roles de aplicación (opcional en access token)

**App roles**: `CLIENTE`, `SUPERVISOR`, `ADMIN` (ya usados en el front).

**Enterprise applications** → tu app → **Users and groups** → asignar roles.

Para que **roles** lleguen al gateway en `X-User-Roles`:

- **Token configuration** → **Add optional claim** → **Access** token → claim **`roles`** (si el asistente lo ofrece para app roles).

El **id token** del front ya puede llevar roles para guards; el **access token** hacia el API depende de esta configuración.

### 1.5 Checklist rápido Azure (demo oral)

1. Mostrar **App registration** (client id, tenant id).
2. Mostrar **API permissions** con consent **Granted**.
3. Login en **Vercel** → F12 → request a `/api/...` con header **Authorization: Bearer**.
4. Token inválido (Bearer `abc`) → **401** del gateway Render.

---

## Parte 2 — Nube Mil Sabores (Render + Vercel) — recomendado para el curso

### 2.1 Backend (Render Blueprint)

Repo backend, rama desplegada, archivo **`render.yaml`**.

1. Render Dashboard → **New → Blueprint** → conectar repo.
2. Completar **DATABASE_*** (Neon) en los 4 microservicios de datos.
3. En **milsabores-api-gateway** (Dashboard → Environment), definir:

| Variable | Valor |
|----------|--------|
| `AZURE_ENTRA_ENABLED` | `true` |
| `AZURE_TENANT_ID` | igual que `VITE_AZURE_TENANT_ID` |
| `AZURE_CLIENT_ID` | igual que `VITE_AZURE_CLIENT_ID` |
| `AZURE_API_AUDIENCE` | `api://<client-id>` |
| `FRONTEND_URL` | URL HTTPS de Vercel |

4. URL pública del gateway (ej. `https://milsabores-api-gateway.onrender.com`) = **API Manager del proyecto** para la presentación.

Health: `GET https://milsabores-api-gateway.onrender.com/actuator/health`

### 2.2 Frontend (Vercel)

**Settings → Environment Variables** (Production):

| Variable | Valor |
|----------|--------|
| `VITE_AZURE_TENANT_ID` | tenant |
| `VITE_AZURE_CLIENT_ID` | client id |
| `VITE_AZURE_AUTHORITY` | `https://login.microsoftonline.com/<tenant>` |
| `VITE_AZURE_API_SCOPE` | `api://<client-id>/access_as_user` |
| `BACKEND_URL` | `https://milsabores-api-gateway.onrender.com` |

En **producción** el front usa rutas relativas **`/api`** (Vercel serverless proxy en `api/*.js`) → Render gateway. No hace falta `VITE_API_GATEWAY_URL` en Vercel si usas ese proxy.

Redeploy front tras cambiar variables.

### 2.3 Demo en vivo (guion)

1. **Instancia API en cloud**: abrir Render → servicio **milsabores-api-gateway** en ejecución.
2. **Rutas al backend**: Swagger o `GET /api/productos` vía gateway (público sin token).
3. **Front vía API Manager**: sitio Vercel → catálogo / login Microsoft.
4. **JWT válido vs inválido**:
   - Con sesión: `GET /api/usuarios/me` → 200 + JSON con `id`.
   - Postman al gateway: `Authorization: Bearer basura` → **401** JSON `Token inválido`.
5. **Login OAuth**: MSAL en el navegador (Entra).

---

## Parte 3 — AWS API Gateway (demo “API Manager” AWS)

Solo si el docente pide **consola AWS** explícitamente. No sustituye Entra (sigue en Azure AD).

### Opción simple (HTTP API como proxy)

1. **AWS Console** → **API Gateway** → **Create API** → **HTTP API**.
2. **Integrations** → **Add integration** → HTTP → URL:
   `https://milsabores-api-gateway.onrender.com/{proxy}`
3. **Routes**: `ANY /api/{proxy+}` → integración anterior.
4. **Stages** → `$default` → URL invoke: `https://xxxx.execute-api.us-east-1.amazonaws.com/api/productos`
5. Explicación oral: el **JWT lo valida** el Spring Gateway en Render al recibir el `Authorization`; AWS enruta tráfico HTTPS. (Validación JWT nativa en API Gateway con Entra requiere **JWT authorizer** con issuer Microsoft o Lambda custom.)

### Demo JWT en AWS (opcional avanzado)

- **HTTP API** → **Authorization** → **JWT authorizer**
- Issuer: `https://login.microsoftonline.com/<tenant-id>/v2.0`
- Audience: `api://<client-id>` o client id según emisión del token.

Si el authorizer AWS y el Spring gateway validan lo mismo, redundante pero válido para la rúbrica “API Manager rechaza inválidos”.

Referencia IaC mínima: `infra/aws/http-api-proxy/README.md` (solo documentación de consola).

---

## Parte 4 — Qué no subir a Git

- Secretos Neon, `JWT_SECRET`, valores reales de tenant en repos públicos.
- Configurar secretos solo en **Render / Vercel / `.env` local**.

---

## Relación con pasos B1–B5

| Paso | B6 |
|------|-----|
| B1–B3 Entra en gateway | Activar `AZURE_*` en Render |
| B4 Headers | Visibles en logs downstream (no en cliente) |
| B5 `/me` | Probar en `https://...onrender.com/api/usuarios/me` con Bearer MSAL |

---

## Rama Git

Trabajo infra/documentación: **`feature/entra-neon-identidad`** (backend). Front: variables en Vercel, no necesariamente commit.
