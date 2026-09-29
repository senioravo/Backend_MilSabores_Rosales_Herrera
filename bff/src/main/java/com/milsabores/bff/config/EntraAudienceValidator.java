package com.milsabores.bff.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class EntraAudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final Set<String> acceptedAudiences;

    EntraAudienceValidator(String audience, String clientId) {
        Set<String> accepted = new LinkedHashSet<>();
        addIfPresent(accepted, audience);
        addIfPresent(accepted, clientId);
        if (audience != null && !audience.isBlank()) {
            String aud = audience.trim();
            addIfPresent(accepted, aud + "/access_as_user");
            if (aud.startsWith("api://")) {
                addIfPresent(accepted, aud.substring("api://".length()));
            }
        }
        this.acceptedAudiences = accepted.stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> tokenAudiences = jwt.getAudience();
        if (tokenAudiences == null || tokenAudiences.isEmpty()) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "El token Entra no incluye audience (aud)", null));
        }
        if (tokenAudiences.stream().anyMatch(acceptedAudiences::contains)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "Audience Entra no válida para el BFF", null));
    }

    private static void addIfPresent(Set<String> target, String value) {
        if (value != null && !value.isBlank()) {
            target.add(value.trim());
        }
    }
}
