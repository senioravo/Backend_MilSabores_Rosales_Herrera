package com.milsabores.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayRoutePolicyTest {

    @Test
    void productosAndCategoriasArePublic() {
        assertTrue(GatewayRoutePolicy.isPublicPath("/api/productos"));
        assertTrue(GatewayRoutePolicy.isPublicPath("/api/productos/P001"));
        assertTrue(GatewayRoutePolicy.isPublicPath("/api/categorias/1"));
    }

    @Test
    void loginRegisterArePublic() {
        assertTrue(GatewayRoutePolicy.isPublicPath("/api/usuarios/login"));
        assertTrue(GatewayRoutePolicy.isPublicPath("/api/usuarios/register"));
    }

    @Test
    void protectedRoutesRequireJwt() {
        assertFalse(GatewayRoutePolicy.isPublicPath("/api/usuarios/me"));
        assertFalse(GatewayRoutePolicy.isPublicPath("/api/carritos/usuario/1"));
        assertFalse(GatewayRoutePolicy.isPublicPath("/bff/me"));
    }

    @Test
    void optionsAllowedWithoutJwtEvenOnProtectedPaths() {
        assertTrue(GatewayRoutePolicy.isAnonymousAllowed(HttpMethod.OPTIONS, "/api/usuarios/me"));
    }
}
