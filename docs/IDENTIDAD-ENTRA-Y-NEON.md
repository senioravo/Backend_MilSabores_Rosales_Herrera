# Identidad Entra ID + datos en Neon (Mil Sabores)

Documento de alineación EP1 — **estado revisado sobre rama `main`** (gateway + BFF + 4 microservicios).

## Resumen

| Sistema | Rol |
|---------|-----|
| **Microsoft Entra ID** | **Autenticación** (login Microsoft) y **autorización** (app roles: `CLIENTE`, `SUPERVISOR`, `ADMIN`) en el token. |
| **Neon PostgreSQL** | **Datos del negocio**: productos, categorías, carrito, ventas y tabla **`usuarios`** con **`id` numérico** usado por carrito/BFF/checkout. |

Entra **no reemplaza** Neon. Neon **no reemplaza** Entra.

## Cómo está hoy el backend (`main`)

1. **usuario-service**: `POST /register`, `POST /login` → BCrypt + **JWT propio** (HS256, `JWT_SECRET`).
2. **api-gateway :8080**: valida JWT (Entra MSAL o legacy usuario-service) y reenvía headers de identidad (`X-User-Email`, `X-Entra-Oid`, `X-User-Roles`, …; legacy incluye `X-User-Id`).
3. **carrito-service / ventas-service / BFF**: operan con **`usuarioId` (Long)** que apunta a filas en **`usuarios`** (Neon).
4. **Frontend MSAL (Entra)**: login Microsoft en el navegador; **aún no** conectado a gateway con token Entra.

No hay código Azure/Entra en Java en `main` (README marca integración IDaaS como pendiente).

## Qué significa “usuario” en cada lugar

### En Entra (Azure portal)

- Lista **Usuarios** del tenant + asignación de **app roles** en *Enterprise application*.
- Admin “ve usuarios” del directorio y quién tiene `CLIENTE` / `SUPERVISOR` / `ADMIN`.
- **No** guarda pedidos ni carritos.

### En Neon (`usuarios`)

- Perfil **de negocio**: `id`, `nombre`, `email`, `password` (hash), `activo`.
- **Carrito** (`carrito_items.usuario_id`) y **ventas** referencian este **`id`**.
- **BFF checkout** (`CheckoutServiceImpl`) llama `usuarioClient.obtenerPorId(usuarioId)` antes de crear la venta.
- `GET /api/usuarios` (protegido por gateway) lista filas de **Neon** — útil para un panel admin de “clientes registrados en la app”, no para listar el directorio Entra completo.

### Registro web clásico (`/register`)

- Sigue creando fila en **Neon** + JWT viejo.
- Con EP1, el camino principal pasa a ser **Microsoft**; el registro local puede quedar solo para desarrollo o deprecarse.

## Modelo objetivo (lógico EP1)

```
Usuario inicia sesión → Entra (MSAL, roles en token)
        ↓
Frontend envía access token Entra → API Gateway (validación Azure JWT — pendiente)
        ↓
BFF o usuario-service: "provisionar / resolver" perfil Neon por email u oid Entra
        ↓
Respuesta incluye neonUserId (Long) para carrito y checkout
        ↓
Carrito / ventas / BFF siguen usando usuarioId en Neon
```

**Roles (`CLIENTE`, etc.)**: se leen del **token Entra** en gateway/BFF y en guards del front. **No** hace falta duplicar roles en columnas de Neon para EP1 básico.

## Qué puede hacer un ADMIN (coherente con el backend actual)

| Acción | Dónde | API / dato |
|--------|-------|------------|
| Ver clientes con perfil en la app | Neon | `GET /api/usuarios` (rol admin en token cuando gateway valide Entra) |
| Gestionar productos / stock | Neon | producto-service |
| Ver / operar ventas | Neon | ventas-service, BFF |
| Asignar quién es supervisor en la organización | Entra | Portal Azure (no Neon) |

## Trabajo pendiente (orden sugerido)

1. **Gateway**: validar JWT de Entra (issuer, audience, JWKS) en lugar de (o además de) JWT `usuario-service`.
2. **BFF o usuario-service**: endpoint `GET /api/usuarios/me` y `GET /bff/me` — sync Neon por headers Entra (**implementado en rama `feature/entra-neon-identidad`**).
3. **Frontend**: tras login MSAL, `syncNeonProfileFromApi()` guarda `id` Neon para carrito.
4. **Opcional schema**: columna `entra_oid` en `usuarios` (**añadida**, ddl-auto update).

## Rama de trabajo

Cambios de código backend para lo anterior: rama **`feature/entra-neon-identidad`** desde `main` (este documento). Implementación Java/migraciones: coordinar con gateway (Diego) antes de merge.

## B6 — Cloud / Azure consent / API Manager

Ver **`docs/B6-CLOUD-AZURE-AWS-API-MANAGER.md`** (Azure Portal, Render, Vercel, demo AWS API Gateway).
