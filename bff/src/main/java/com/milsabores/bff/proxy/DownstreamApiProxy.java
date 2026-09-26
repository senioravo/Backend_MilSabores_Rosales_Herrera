package com.milsabores.bff.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

/**
 * Reenvía /api/** al microservicio correspondiente (orquestación EP1 vía BFF).
 */
@Component
public class DownstreamApiProxy {

    private static final Set<String> FORWARD_HEADER_PREFIXES = Set.of(
            "x-auth-provider",
            "x-user-",
            "x-entra-",
            "content-type",
            "accept");

    private final String usuarioBase;
    private final String productoBase;
    private final String carritoBase;
    private final String ventasBase;
    private final RestClient restClient;

    public DownstreamApiProxy(
            @Value("${services.usuario.url}") String usuarioBase,
            @Value("${services.producto.url}") String productoBase,
            @Value("${services.carrito.url}") String carritoBase,
            @Value("${services.ventas.url}") String ventasBase) {
        this.usuarioBase = trimTrailingSlash(usuarioBase);
        this.productoBase = trimTrailingSlash(productoBase);
        this.carritoBase = trimTrailingSlash(carritoBase);
        this.ventasBase = trimTrailingSlash(ventasBase);
        this.restClient = RestClient.create();
    }

    public ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] body) {
        String path = request.getRequestURI();
        String base = resolveBaseUrl(path);
        if (base == null) {
            return ResponseEntity.notFound().build();
        }

        String query = request.getQueryString();
        String target = base + path + (query != null && !query.isBlank() ? "?" + query : "");
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        RestClient.RequestBodySpec spec = restClient
                .method(method)
                .uri(target)
                .headers(headers -> copyForwardHeaders(request, headers));

        RestClient.ResponseSpec responseSpec;
        if (body != null && body.length > 0 && allowsBody(method)) {
            responseSpec = spec.body(body).retrieve();
        } else {
            responseSpec = spec.retrieve();
        }

        return responseSpec.toEntity(byte[].class);
    }

    private String resolveBaseUrl(String path) {
        if (path.startsWith("/api/usuarios")) {
            return usuarioBase;
        }
        if (path.startsWith("/api/productos") || path.startsWith("/api/categorias")) {
            return productoBase;
        }
        if (path.startsWith("/api/carritos")) {
            return carritoBase;
        }
        if (path.startsWith("/api/ventas")) {
            return ventasBase;
        }
        return null;
    }

    private static void copyForwardHeaders(HttpServletRequest request, HttpHeaders target) {
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) {
            return;
        }
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!shouldForwardHeader(name)) {
                continue;
            }
            Collections.list(request.getHeaders(name)).forEach(value -> target.add(name, value));
        }
    }

    private static boolean shouldForwardHeader(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase();
        return FORWARD_HEADER_PREFIXES.stream().anyMatch(lower::startsWith);
    }

    private static boolean allowsBody(HttpMethod method) {
        return method == HttpMethod.POST
                || method == HttpMethod.PUT
                || method == HttpMethod.PATCH;
    }

    private static String trimTrailingSlash(String url) {
        if (url == null) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
