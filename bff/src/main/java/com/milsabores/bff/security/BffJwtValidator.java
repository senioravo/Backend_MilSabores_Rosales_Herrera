package com.milsabores.bff.security;

import com.milsabores.bff.config.AzureEntraProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Segunda validación JWT (guía EP1): independiente del API Gateway.
 * Re-decodifica el Bearer (Entra RS256 o legacy HS256) antes de ejecutar /bff/**.
 */
@Component
public class BffJwtValidator {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final AzureEntraProperties entraProperties;
    private final JwtDecoder entraJwtDecoder;

    public BffJwtValidator(
            AzureEntraProperties entraProperties,
            @Autowired(required = false) @Qualifier("entraJwtDecoder") JwtDecoder entraJwtDecoder) {
        this.entraProperties = entraProperties;
        this.entraJwtDecoder = entraJwtDecoder;
    }

    /**
     * Valida el token y deja {@link BffAuthenticationToken} en el {@link SecurityContextHolder}.
     *
     * @throws JwtException si el token no es válido en ningún modo
     */
    public void validateAndAuthenticate(HttpServletRequest request, String token) {
        if (useEntraDecoder()) {
            try {
                Jwt jwt = entraJwtDecoder.decode(token);
                authenticateEntra(request, jwt);
                return;
            } catch (JwtException ex) {
                // fallback legacy (misma estrategia que api-gateway)
            }
        }
        authenticateLegacy(token);
    }

    private boolean useEntraDecoder() {
        return entraProperties.isConfigured() && entraJwtDecoder != null;
    }

    private void authenticateEntra(HttpServletRequest request, Jwt jwt) {
        String email = firstNonBlank(
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("upn"),
                jwt.getSubject());
        String roles = request.getHeader("X-User-Roles");
        if (roles == null || roles.isBlank()) {
            Object rolesClaim = jwt.getClaims().get("roles");
            if (rolesClaim instanceof Iterable<?> iterable) {
                StringBuilder sb = new StringBuilder();
                for (Object r : iterable) {
                    if (r != null && !r.toString().isBlank()) {
                        if (!sb.isEmpty()) {
                            sb.append(',');
                        }
                        sb.append(r.toString().trim());
                    }
                }
                roles = sb.toString();
            }
        }
        SecurityContextHolder.getContext()
                .setAuthentication(new BffAuthenticationToken(email, "entra", roles));
    }

    private void authenticateLegacy(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (claims.getExpiration().before(new Date())) {
            throw new JwtException("Token expirado");
        }
        SecurityContextHolder.getContext()
                .setAuthentication(new BffAuthenticationToken(claims.getSubject(), "legacy", null));
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return "usuario";
    }
}
