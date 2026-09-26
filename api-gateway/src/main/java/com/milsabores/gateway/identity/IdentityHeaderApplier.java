package com.milsabores.gateway.identity;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Map;

public final class IdentityHeaderApplier {

    private IdentityHeaderApplier() {}

    public static ServerHttpRequest apply(ServerHttpRequest request, Map<String, String> headers) {
        ServerHttpRequest.Builder builder = request.mutate();
        headers.forEach(builder::header);
        return builder.build();
    }
}
