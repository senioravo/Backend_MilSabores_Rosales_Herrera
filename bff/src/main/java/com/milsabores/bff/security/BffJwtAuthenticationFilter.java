package com.milsabores.bff.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Segunda validación JWT en /bff/** y en rutas /api/** protegidas (misma política que el gateway).
 */
@Component
public class BffJwtAuthenticationFilter extends OncePerRequestFilter {

    private final BffJwtValidator jwtValidator;

    public BffJwtAuthenticationFilter(BffJwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")) {
            return true;
        }
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        return BffPublicRoutePolicy.isAnonymousAllowed(method, path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.startsWith("/bff") && !path.startsWith("/api")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            unauthorized(response, "Falta Authorization Bearer en el BFF");
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            unauthorized(response, "Bearer token vacío");
            return;
        }

        try {
            jwtValidator.validateAndAuthenticate(request, token);
            filterChain.doFilter(request, response);
        } catch (JwtException | io.jsonwebtoken.JwtException ex) {
            unauthorized(response, "Token inválido en el BFF (segunda validación)");
        } catch (Exception ex) {
            unauthorized(response, "Token inválido en el BFF");
        }
    }

    private static void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + message.replace("\"", "\\\"") + "\"}");
    }
}
