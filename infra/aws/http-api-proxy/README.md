# AWS HTTP API — proxy hacia Mil Sabores (demo B6)

No es despliegue automático; guía de consola para la presentación.

## Objetivo

Mostrar una **instancia de API Gateway** en AWS que expone rutas públicas hacia el backend ya desplegado en Render:

`https://milsabores-api-gateway.onrender.com`

## Pasos (consola)

1. Región: **us-east-1** (o la que use el curso).
2. **API Gateway** → **Create API** → **HTTP API** → Build.
3. **Add integration**:
   - Type: **HTTP URI**
   - URL base: `https://milsabores-api-gateway.onrender.com`
4. **Add route**:
   - Method: **ANY**
   - Path: `/api/{proxy+}`
   - Integration: la HTTP creada (mapear `{proxy}` al path downstream si el asistente lo pide).
5. **Deploy** → stage `$default`.
6. Copiar **Invoke URL** y probar:
   - `GET .../api/productos` (público)
   - `GET .../api/carritos/...` con `Authorization: Bearer <token Entra>` → validación en Render.

## JWT en la demo

- **Recomendado curso:** decir que la **validación Entra** ocurre en el **api-gateway Spring** (Mil Sabores B3).
- **Extra AWS:** JWT authorizer con issuer `https://login.microsoftonline.com/<TENANT_ID>/v2.0` y audience acorde a Azure.

## Costo

HTTP API tiene tier gratuito limitado; apagar o borrar la API tras la presentación si no se usará.
