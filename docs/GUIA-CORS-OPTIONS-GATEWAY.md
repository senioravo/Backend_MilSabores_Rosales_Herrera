# Guía — CORS y ruta OPTIONS (API Gateway)

Ítem del curso en **`api-gateway`** (rama `feature/entra-neon-identidad`).

## Por qué solo en el gateway

El frontend (Vite `:5173` o Vercel HTTPS) llama al **único origen público** del backend. Si cada microservicio aplicara CORS distinto, en producción verías **403** o **headers duplicados**. Por eso Diego centralizó CORS aquí y el gateway **quita `Origin`** antes de reenviar (`RemoveRequestHeader=Origin`).

## Qué está configurado

| Pieza | Función |
|-------|---------|
| `application.yml` → `globalcors` | Orígenes, métodos, headers, `allowCredentials`, `maxAge` |
| `add-to-simple-url-handler-mapping: true` | OPTIONS responde CORS aun en preflight temprano |
| `GatewayCorsConfig` + `GatewaySecurityConfig.cors()` | Spring Security no bloquea preflight |
| `GatewayRoutePolicy` + `JwtAuthenticationFilter` | **OPTIONS no exige JWT** (antes del Bearer real) |
| `FRONTEND_URL` | Origen Vercel en AWS/Render (env) |

### Orígenes permitidos

- `http://localhost:5173` (dev)
- `http://localhost:3000`
- `${FRONTEND_URL}` (prod, ej. Vercel)

### Headers permitidos (incluye MSAL)

`Authorization`, `Content-Type`, `Accept`, `Origin`, `X-Requested-With`

## Flujo preflight (OPTIONS)

1. Navegador envía **OPTIONS** con `Origin` + `Access-Control-Request-Method`.
2. Gateway responde **200/204** con `Access-Control-Allow-*` **sin** pedir Bearer.
3. Navegador envía **GET/POST** con `Authorization: Bearer …` → ahí actúa JWT (guía anterior).

## Demo presentación

Con gateway en `:8080`:

```bash
curl -i -X OPTIONS "http://localhost:8080/api/usuarios/me" ^
  -H "Origin: http://localhost:5173" ^
  -H "Access-Control-Request-Method: GET" ^
  -H "Access-Control-Request-Headers: authorization,content-type"
```

Debes ver `Access-Control-Allow-Origin: http://localhost:5173` y **no** 401.

En DevTools → Network, al llamar API desde el front verás el OPTIONS previo en rutas “no simples”.

## AWS

Al desplegar el repo en AWS, define **`FRONTEND_URL=https://tu-app.vercel.app`** en el contenedor/EC2 del **api-gateway**. No hace falta abrir CORS en cada microservicio interno.

## Tests

```bash
cd api-gateway
./gradlew test
```

`GatewayCorsConfigTest` — orígenes 5173/Vercel y header `Authorization`.

`JwtAuthenticationFilterIntegrationTest` — OPTIONS en ruta protegida no devuelve 401.

## Archivos

- `src/main/resources/application.yml` (globalcors)
- `config/GatewayCorsConfig.java`
- `config/GatewaySecurityConfig.java`
- `security/GatewayRoutePolicy.java` (OPTIONS sin JWT)
