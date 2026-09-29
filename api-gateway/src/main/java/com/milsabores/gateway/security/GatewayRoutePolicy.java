package com.milsabores.gateway.security;

import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * Política de acceso del api-gateway (guía: validación JWT + CORS/OPTIONS).
 * Rutas públicas sin Bearer; el resto exige JWT Entra o legacy.
 */
public final class GatewayRoutePolicy {

    public static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/usuarios/login",
            "/api/usuarios/register",
            "/api/usuarios/registro",
            "/api/productos",
            "/api/categorias",
            "/api/ventas/transbank/return",
            "/actuator");

    private GatewayRoutePolicy() {}

    public static boolean isPublicPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    /** CORS preflight: no exige JWT (guía: OPTIONS para el frontend). */
    public static boolean isAnonymousAllowed(HttpMethod method, String path) {
        return method == HttpMethod.OPTIONS || isPublicPath(path);
    }
}
