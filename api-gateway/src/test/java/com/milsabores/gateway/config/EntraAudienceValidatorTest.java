package com.milsabores.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntraAudienceValidatorTest {

    @Test
    void acceptsConfiguredApiAudience() {
        var validator = new EntraAudienceValidator("api://my-api", "client-guid");
        Jwt jwt = jwtWithAud("api://my-api");
        assertFalse(validator.validate(jwt).hasErrors());
    }

    @Test
    void acceptsClientIdAsAudience() {
        var validator = new EntraAudienceValidator("api://my-api", "client-guid");
        Jwt jwt = jwtWithAud("client-guid");
        assertFalse(validator.validate(jwt).hasErrors());
    }

    @Test
    void acceptsScopeStyleAudience() {
        var validator = new EntraAudienceValidator("api://my-api", "client-guid");
        Jwt jwt = jwtWithAud("api://my-api/access_as_user");
        assertFalse(validator.validate(jwt).hasErrors());
    }

    @Test
    void rejectsUnknownAudience() {
        var validator = new EntraAudienceValidator("api://my-api", "client-guid");
        Jwt jwt = jwtWithAud("other");
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertTrue(result.hasErrors());
    }

    private static Jwt jwtWithAud(String aud) {
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of("aud", List.of(aud), "sub", "user"));
    }
}
