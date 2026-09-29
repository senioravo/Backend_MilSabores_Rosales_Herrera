# Guía — Validación JWT en API Gateway

Ítem del curso cumplido en **`api-gateway`** (rama `feature/entra-neon-identidad`).

## Qué hace el gateway

1. **Punto único de entrada** (`:8080` local, Render/AWS en producción).
2. **`JwtAuthenticationFilter`** (GlobalFilter):
   - **OPTIONS** y rutas **públicas** → sin JWT (CORS preflight + catálogo).
   - Resto → header **`Authorization: Bearer …`** obligatorio.
3. **Dos tipos de token aceptados** (cuando `AZURE_ENTRA_ENABLED=true`):
   - **Entra / MSAL** (RS256): `ReactiveJwtDecoder` + issuer JWKS + audience (`AZURE_*`).
   - **Legacy** usuario-service (HS256): mismo `JWT_SECRET` que firma el login email/password.
4. Token inválido o ausente → **401** JSON `{"error":"…"}`.
5. Token válido → inyecta headers (`X-User-Email`, `X-Entra-Oid`, `X-User-Roles`, o `X-User-Id` legacy) hacia microservicios.

## Rutas públicas (sin JWT)

Definidas en `GatewayRoutePolicy`:

- `/api/usuarios/login`, `/register`, `/registro`
- `/api/productos/**`, `/api/categorias/**`
- `/api/ventas/transbank/return`
- `/actuator/**`

Protegidas (ejemplos): `/api/usuarios/me`, `/api/carritos/**`, `/api/ventas/**`, `/bff/**`.

## Archivos clave

| Archivo | Rol |
|---------|-----|
| `filter/JwtAuthenticationFilter.java` | Filtro global JWT |
| `config/AzureEntraJwtDecoderConfig.java` | Decoder Entra (B2) |
| `security/GatewayRoutePolicy.java` | Rutas públicas + OPTIONS |
| `identity/EntraIdentityHeaderMapper.java` | Claims → headers |

## Cómo demostrar (presentación)

```bash
# Sin token → 401
curl -i http://localhost:8080/api/usuarios/me

# Token basura → 401
curl -i -H "Authorization: Bearer abc" http://localhost:8080/api/carritos/usuario/1

# Público → 200/502 según downstream (no 401 por JWT)
curl -i http://localhost:8080/api/productos

# Con MSAL (token real desde front DevTools)
curl -i -H "Authorization: Bearer <access_token>" http://localhost:8080/api/usuarios/me
```

Variables locales: `.env` con `JWT_SECRET`, `AZURE_ENTRA_ENABLED`, `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `AZURE_API_AUDIENCE`.

## Tests automáticos

```bash
cd api-gateway
./gradlew test
```

- `GatewayRoutePolicyTest` — rutas públicas vs protegidas.
- `JwtAuthenticationFilterIntegrationTest` — 401 sin Bearer, OPTIONS permitido.
- `EntraAudienceValidatorTest` — audience Entra.

## Relación con Azure / AWS

- **Azure:** emite el access token que el gateway valida (no valida en Azure Portal).
- **AWS:** despliegan el mismo `api-gateway`; la validación sigue en este filtro (API Gateway AWS opcional solo enruta tráfico).
