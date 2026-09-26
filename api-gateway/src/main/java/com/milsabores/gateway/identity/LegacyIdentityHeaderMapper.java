package com.milsabores.gateway.identity;

import io.jsonwebtoken.Claims;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.milsabores.gateway.identity.GatewayIdentityHeaders.*;

/**
 * Headers para JWT legacy firmado por usuario-service (HS256).
 */
public final class LegacyIdentityHeaderMapper {

    private LegacyIdentityHeaderMapper() {}

    public static Map<String, String> toHeaders(Claims claims) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(AUTH_PROVIDER, PROVIDER_LEGACY);
        headers.put(USER_EMAIL, claims.getSubject());

        Object usuarioId = claims.get("usuarioId");
        if (usuarioId != null) {
            headers.put(USER_ID, String.valueOf(usuarioId));
        }

        Object nombre = claims.get("nombre");
        if (nombre != null && !String.valueOf(nombre).isBlank()) {
            headers.put(USER_NAME, String.valueOf(nombre).trim());
        }

        return headers;
    }
}
