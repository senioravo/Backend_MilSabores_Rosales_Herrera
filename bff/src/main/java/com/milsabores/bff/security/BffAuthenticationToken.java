package com.milsabores.bff.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Principal autenticado en el BFF tras validar JWT (Entra o legacy).
 */
public class BffAuthenticationToken extends AbstractAuthenticationToken {

    private final String subject;
    private final String authProvider;

    public BffAuthenticationToken(String subject, String authProvider, String rolesCsv) {
        super(parseRoles(rolesCsv));
        this.subject = subject;
        this.authProvider = authProvider;
        setAuthenticated(true);
    }

    private static List<SimpleGrantedAuthority> parseRoles(String rolesCsv) {
        if (rolesCsv == null || rolesCsv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(rolesCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return subject;
    }

    public String getAuthProvider() {
        return authProvider;
    }
}
