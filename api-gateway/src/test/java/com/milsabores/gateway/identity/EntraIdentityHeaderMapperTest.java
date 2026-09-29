package com.milsabores.gateway.identity;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntraIdentityHeaderMapperTest {

    @Test
    void mapsStandardEntraClaimsToHeaders() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of(
                        "sub", "abc-sub",
                        "oid", "oid-123",
                        "tid", "tenant-456",
                        "preferred_username", "maria@contoso.com",
                        "name", "María",
                        "roles", List.of("CLIENTE", "ADMIN"),
                        "scp", "access_as_user"));

        Map<String, String> headers = EntraIdentityHeaderMapper.toHeaders(jwt);

        assertEquals("entra", headers.get(GatewayIdentityHeaders.AUTH_PROVIDER));
        assertEquals("maria@contoso.com", headers.get(GatewayIdentityHeaders.USER_EMAIL));
        assertEquals("María", headers.get(GatewayIdentityHeaders.USER_NAME));
        assertEquals("oid-123", headers.get(GatewayIdentityHeaders.ENTRA_OID));
        assertEquals("abc-sub", headers.get(GatewayIdentityHeaders.ENTRA_SUB));
        assertEquals("tenant-456", headers.get(GatewayIdentityHeaders.ENTRA_TENANT_ID));
        assertEquals("CLIENTE,ADMIN", headers.get(GatewayIdentityHeaders.USER_ROLES));
        assertEquals("access_as_user", headers.get(GatewayIdentityHeaders.ENTRA_SCOPES));
        assertTrue(!headers.containsKey(GatewayIdentityHeaders.USER_ID));
    }
}
