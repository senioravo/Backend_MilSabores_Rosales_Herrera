package com.milsabores.bff.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

/**
 * Misma política de rutas públicas que el API Gateway (catálogo, login legacy, retorno Transbank).
 */
public final class BffPublicRoutePolicy {

    public static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/usuarios/login",
            "/api/usuarios/register",
            "/api/usuarios/registro",
            "/api/productos",
            "/api/categorias",
            "/api/ventas/transbank/return");

    private BffPublicRoutePolicy() {}

    public static boolean isPublicPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    public static boolean isAnonymousAllowed(HttpMethod method, String path) {
        return method == HttpMethod.OPTIONS || isPublicPath(path);
    }

    public static RequestMatcher publicPathMatcher() {
        return request -> isPublicPath(request.getRequestURI());
    }
}
