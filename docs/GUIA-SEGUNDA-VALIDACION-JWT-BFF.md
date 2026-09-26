# Guía — Segunda validación del JWT en el BFF

Ítem del curso cumplido en **`bff`** (rama `feature/entra-neon-identidad`).

## Por qué hay dos validaciones

| Capa | Puerto (local) | Responsabilidad |
|------|----------------|-----------------|
| **API Gateway** | `:8080` | Primera línea: todo `/bff/**` exige Bearer; valida Entra o legacy; reenvía `Authorization` + headers de identidad al BFF. |
| **BFF** | `:8085` | **Segunda validación:** vuelve a decodificar el mismo Bearer **sin confiar** en que “ya pasó” el gateway. |

Motivos pedagógicos y de seguridad:

1. **Defensa en profundidad** — En Render/AWS el BFF tiene URL pública (`milsabores-bff.onrender.com`). Un atacante puede saltarse el gateway; el BFF sigue exigiendo JWT válido.
2. **Mismo criterio criptográfico** — Entra (JWKS + audience) o legacy (`JWT_SECRET` HS256), alineado con `JwtAuthenticationFilter` del gateway.
3. **Separación de responsabilidades** — El gateway enruta y aplica política global; el BFF asegura su propia superficie `/bff/**` antes de agregar checkout o `/bff/me`.

```text
Front (MSAL) ──Bearer──► API Gateway ──Bearer + X-User-*──► BFF ──► microservicios
                              │                              │
                         1ª validación JWT              2ª validación JWT
                         (JwtAuthenticationFilter)      (BffJwtValidator)
```

## Implementación

1. **`BffJwtAuthenticationFilter`** — En rutas `/bff/**` exige header `Authorization: Bearer …`.
2. **`BffJwtValidator`** — Segunda validación:
   - Si `AZURE_ENTRA_ENABLED=true` y `AZURE_*` OK → `entraJwtDecoder.decode(token)`; si falla → fallback legacy.
   - Legacy → `Jwts.parser()` con `jwt.secret` / `JWT_SECRET`, comprueba expiración.
   - Éxito → `BffAuthenticationToken` en `SecurityContext`.
3. **401** si falta Bearer o la segunda validación falla: `{"error":"Token inválido en el BFF (segunda validación)"}` (u otro mensaje del filtro).

Los headers `X-User-Email`, `X-Entra-Oid`, etc. los usa el controlador para llamar a `usuario-service`; **no sustituyen** al JWT: sin Bearer válido no se llega al controller.

## Archivos clave

| Archivo | Rol |
|---------|-----|
| `security/BffJwtValidator.java` | Segunda decodificación Entra + legacy |
| `security/BffJwtAuthenticationFilter.java` | Exige Bearer en `/bff/**` |
| `config/BffSecurityConfig.java` | `/bff/**` authenticated |
| `api-gateway/.../JwtAuthenticationFilter.java` | Primera validación (referencia) |

## Cómo demostrar (presentación)

**A — Flujo normal (doble validación invisible para el usuario)**

```bash
curl -i -H "Authorization: Bearer <access_token_msal>" http://localhost:8080/bff/me
```

Gateway y BFF validan el mismo token.

**B — Bypass del gateway (solo segunda capa)**

```bash
# Sin token → 401 en BFF aunque inventes headers de identidad
curl -i http://localhost:8085/bff/me \
  -H "X-User-Email: admin@fake.com" \
  -H "X-Entra-Oid: 00000000-0000-0000-0000-000000000000"

# Con token MSAL o legacy válido → pasa la 2ª validación
curl -i http://localhost:8085/bff/me \
  -H "Authorization: Bearer <token>"
```

**C — Token caducado o firmado con otro secreto → 401 en BFF**

## Tests automáticos

```bash
cd bff
./gradlew test
```

| Test | Qué demuestra |
|------|----------------|
| `BffJwtValidatorTest` | Segunda validación legacy: OK / expirado / secreto incorrecto |
| `BffSecurityIntegrationTest` | HTTP 401 sin Bearer; JWT inválido; JWT legacy válido en `/bff/me` |

## Variables (Azure + JWT)

Mismas que el gateway en `.env` del backend:

- `JWT_SECRET`
- `AZURE_ENTRA_ENABLED`, `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `AZURE_API_AUDIENCE`

Render: servicios `milsabores-api-gateway` y `milsabores-bff` con los mismos valores (ver `render.yaml`).

## Documentos relacionados

- [GUIA-VALIDACION-JWT-GATEWAY.md](./GUIA-VALIDACION-JWT-GATEWAY.md) — primera validación
- [GUIA-BFF-SPRING-SECURITY.md](./GUIA-BFF-SPRING-SECURITY.md) — cadena Spring Security del BFF
