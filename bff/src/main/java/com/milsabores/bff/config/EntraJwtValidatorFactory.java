package com.milsabores.bff.config;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.List;

final class EntraJwtValidatorFactory {

    private EntraJwtValidatorFactory() {}

    static OAuth2TokenValidator<Jwt> issuerValidator(String tenantId) {
        String tenant = tenantId.trim();
        List<String> allowedIssuers = List.of(
                "https://login.microsoftonline.com/" + tenant + "/v2.0",
                "https://login.microsoftonline.com/" + tenant + "/",
                "https://sts.windows.net/" + tenant + "/");

        return jwt -> {
            String iss = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
            if (allowedIssuers.stream().anyMatch(iss::equals)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Issuer no válido: " + iss, null));
        };
    }

    static JwtDecoder buildDecoder(AzureEntraProperties entra) {
        String issuer = entra.getIssuerUri();
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(issuer).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                issuerValidator(entra.getTenantId()),
                new EntraAudienceValidator(entra.getAudience(), entra.getClientId())));
        return decoder;
    }
}
