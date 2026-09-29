package com.milsabores.gateway.identity;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.milsabores.gateway.identity.GatewayIdentityHeaders.*;

/**
 * Paso B4: traduce claims del access token Entra a headers HTTP downstream.
 */
public final class EntraIdentityHeaderMapper {

    private EntraIdentityHeaderMapper() {}

    public static Map<String, String> toHeaders(Jwt jwt) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(AUTH_PROVIDER, PROVIDER_ENTRA);

        putIfPresent(headers, USER_EMAIL, resolveEmail(jwt));
        putIfPresent(headers, USER_NAME, jwt.getClaimAsString("name"));
        putIfPresent(headers, ENTRA_OID, jwt.getClaimAsString("oid"));
        putIfPresent(headers, ENTRA_SUB, jwt.getSubject());
        putIfPresent(headers, ENTRA_TENANT_ID, jwt.getClaimAsString("tid"));

        List<String> roles = extractRoles(jwt);
        if (!roles.isEmpty()) {
            headers.put(USER_ROLES, String.join(",", roles));
        }

        String scopes = jwt.getClaimAsString("scp");
        if (scopes != null && !scopes.isBlank()) {
            headers.put(ENTRA_SCOPES, scopes.trim());
        }

        return headers;
    }

    static String resolveEmail(Jwt jwt) {
        return firstNonBlank(
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("upn"),
                jwt.getSubject());
    }

    static List<String> extractRoles(Jwt jwt) {
        Object rolesClaim = jwt.getClaims().get("roles");
        if (rolesClaim == null) {
            return List.of();
        }
        if (rolesClaim instanceof String role) {
            return role.isBlank() ? List.of() : List.of(role.trim());
        }
        if (rolesClaim instanceof Collection<?> collection) {
            List<String> roles = new ArrayList<>();
            for (Object item : collection) {
                if (item != null) {
                    String text = item.toString().trim();
                    if (!text.isEmpty()) {
                        roles.add(text);
                    }
                }
            }
            return roles;
        }
        return List.of();
    }

    private static void putIfPresent(Map<String, String> headers, String name, String value) {
        if (value != null && !value.isBlank()) {
            headers.put(name, value.trim());
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
