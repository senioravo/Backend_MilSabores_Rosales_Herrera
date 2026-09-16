package com.milsabores.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting simple por IP, en memoria. No usa Redis porque este proyecto
 * corre una unica instancia del gateway (no hay un cluster de gateways que
 * necesite un contador compartido); si eso cambia, esto debe migrar a
 * spring-cloud-gateway RequestRateLimiter con Redis.
 *
 * Se ejecuta antes que JwtAuthenticationFilter (getOrder() mas bajo) para
 * frenar abuso incluso en rutas publicas como /api/usuarios/login.
 */
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    @Value("${rate-limit.requests-per-window:60}")
    private int limit;

    @Value("${rate-limit.window-seconds:60}")
    private int windowSeconds;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String key = resolveClientKey(exchange);
        Window window = windows.computeIfAbsent(key, k -> new Window());

        if (window.tryConsume(limit, windowSeconds)) {
            return chain.filter(exchange);
        }
        return tooManyRequests(exchange);
    }

    private String resolveClientKey(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        byte[] bytes = "{\"error\":\"Demasiadas solicitudes, intenta de nuevo mas tarde\"}"
                .getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    /** Libera entradas de clientes inactivos para no crecer sin limite. */
    @Scheduled(fixedRate = 300_000)
    public void limpiarVentanasInactivas() {
        long now = Instant.now().getEpochSecond();
        windows.entrySet().removeIf(e -> now - e.getValue().windowStartEpochSecond > 600);
    }

    @Override
    public int getOrder() {
        return -2;
    }

    private static class Window {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStartEpochSecond = Instant.now().getEpochSecond();

        synchronized boolean tryConsume(int limit, int windowSeconds) {
            long now = Instant.now().getEpochSecond();
            if (now - windowStartEpochSecond >= windowSeconds) {
                windowStartEpochSecond = now;
                count.set(0);
            }
            return count.incrementAndGet() <= limit;
        }
    }
}
