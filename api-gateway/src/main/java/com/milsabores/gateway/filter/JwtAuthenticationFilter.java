package com.milsabores.gateway.filter;

import com.milsabores.gateway.config.AzureEntraProperties;
import com.milsabores.gateway.identity.EntraIdentityHeaderMapper;
import com.milsabores.gateway.identity.IdentityHeaderApplier;
import com.milsabores.gateway.identity.LegacyIdentityHeaderMapper;
import com.milsabores.gateway.security.GatewayRoutePolicy;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Guía EP1 — Validación JWT en API Gateway.
 * Entra (MSAL) + fallback JWT legacy; rutas públicas en {@link GatewayRoutePolicy}.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    private final AzureEntraProperties entraProperties;

    private final ReactiveJwtDecoder entraReactiveJwtDecoder;

    public JwtAuthenticationFilter(
            AzureEntraProperties entraProperties,
            @Autowired(required = false) @Qualifier("entraReactiveJwtDecoder")
                    ReactiveJwtDecoder entraReactiveJwtDecoder) {
        this.entraProperties = entraProperties;
        this.entraReactiveJwtDecoder = entraReactiveJwtDecoder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (GatewayRoutePolicy.isAnonymousAllowed(request.getMethod(), request.getURI().getPath())) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Falta el header Authorization con un Bearer token");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return unauthorized(exchange, "Bearer token vacío");
        }

        if (useEntraValidation()) {
            return entraReactiveJwtDecoder
                    .decode(token)
                    .flatMap(jwt -> chain.filter(exchange.mutate()
                            .request(IdentityHeaderApplier.apply(
                                    request, EntraIdentityHeaderMapper.toHeaders(jwt)))
                            .build()))
                    .onErrorResume(
                            org.springframework.security.oauth2.jwt.JwtException.class,
                            e -> authenticateLegacy(exchange, chain, token, request));
        }

        return authenticateLegacy(exchange, chain, token, request);
    }

    private boolean useEntraValidation() {
        return entraProperties.isConfigured() && entraReactiveJwtDecoder != null;
    }

    private Mono<Void> authenticateLegacy(
            ServerWebExchange exchange, GatewayFilterChain chain, String token, ServerHttpRequest request) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (claims.getExpiration().before(new Date())) {
                return unauthorized(exchange, "Token expirado");
            }

            ServerHttpRequest mutatedRequest = IdentityHeaderApplier.apply(
                    request, LegacyIdentityHeaderMapper.toHeaders(claims));

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            return unauthorized(exchange, "Token inválido");
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        byte[] bytes = ("{\"error\":\"" + escapeJson(message) + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private static String escapeJson(String message) {
        return message.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
