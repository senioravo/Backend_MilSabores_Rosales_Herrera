# Guía — BFF con Spring Security

Ítem del curso cumplido en el módulo **`bff`** (rama `feature/entra-neon-identidad`, puerto **8085** local).

## Qué hace el BFF

1. **Agregación de checkout** (`/bff/checkout/**`) y **identidad** (`GET /bff/me`) para el frontend vía API Gateway (`/bff/**` → BFF).
2. **Spring Security** (`BffSecurityConfig`):
   - Sesión **stateless**, CSRF desactivado (API REST).
   - **Público:** `/actuator/**`, Swagger/OpenAPI.
   - **`/bff/**`:** autenticado (JWT obligatorio).
   - Cualquier otra ruta → **403 denyAll** (superficie mínima).
3. **`BffJwtAuthenticationFilter`** (antes de `UsernamePasswordAuthenticationFilter`):
   - Solo aplica lógica JWT en rutas `/bff/**` (actuator/swagger se omiten en el filtro).
   - Exige **`Authorization: Bearer …`**; ausente o inválido → **401** JSON `{"error":"…"}`.
   - Con **`AZURE_ENTRA_ENABLED=true`** y `AZURE_*` configurados: valida token **Entra** (mismo decoder que gateway) y, si falla, **fallback legacy** HS256 con `JWT_SECRET`.
   - Rellena `SecurityContext` con `BffAuthenticationToken` (email, origen `entra|legacy`, roles desde claim o header `X-User-Roles`).

## Segunda validación JWT

Documentada en **[GUIA-SEGUNDA-VALIDACION-JWT-BFF.md](./GUIA-SEGUNDA-VALIDACION-JWT-BFF.md)** (`BffJwtValidator` + tests).

## Archivos clave

| Archivo | Rol |
|---------|-----|
| `config/BffSecurityConfig.java` | `SecurityFilterChain`, `@EnableMethodSecurity` |
| `security/BffJwtValidator.java` | Segunda decodificación JWT (Entra + legacy) |
| `security/BffJwtAuthenticationFilter.java` | Exige Bearer en `/bff/**` |
| `security/BffAuthenticationToken.java` | Principal para `@PreAuthorize` (futuro) |
| `config/AzureEntraProperties.java` | Mapeo `AZURE_*` |
| `config/AzureEntraJwtDecoderConfig.java` | Bean `entraJwtDecoder` |
| `config/EntraAudienceValidator.java` | Audience Entra |
| `controller/IdentityController.java` | `GET /bff/me` |

## Cómo demostrar (presentación)

Con BFF levantado en `:8085` (o vía gateway `:8080/bff/...`):

```bash
# Directo al BFF — sin token → 401
curl -i http://localhost:8085/bff/me

# Token inválido → 401
curl -i -H "Authorization: Bearer abc" http://localhost:8085/bff/me

# Health público → 200
curl -i http://localhost:8085/actuator/health

# Vía gateway (flujo real del front)
curl -i -H "Authorization: Bearer <access_token_msal>" http://localhost:8080/bff/me
```

Variables locales (mismas que gateway en `.env` del repo backend): `JWT_SECRET`, `AZURE_ENTRA_ENABLED`, `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `AZURE_API_AUDIENCE`, más URLs de microservicios.

## Tests automáticos

```bash
cd bff
./gradlew test
```

- `BffSecurityIntegrationTest` — 401 en `/bff/me` sin Bearer o con token inválido; actuator health público.

## Relación con Azure / AWS / Render

- **Azure:** el access token que el usuario obtiene con MSAL es el que el BFF decodifica (issuer JWKS del tenant).
- **Render:** en `render.yaml`, servicio `milsabores-bff` debe tener `JWT_SECRET` (grupo compartido) y las mismas claves `AZURE_*` que el gateway cuando Entra esté activo en producción.
- **AWS:** mismo artefacto Docker del BFF; Spring Security no cambia entre Render y AWS, solo variables de entorno y URLs de downstream.
