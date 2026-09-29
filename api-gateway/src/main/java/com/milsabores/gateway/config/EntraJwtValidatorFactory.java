package com.milsabores.gateway.config;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;

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

    static void configureDecoder(NimbusReactiveJwtDecoder decoder, AzureEntraProperties entra) {
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                issuerValidator(entra.getTenantId()),
                new EntraAudienceValidator(entra.getAudience(), entra.getClientId())));
    }

    static ReactiveJwtDecoder buildDecoder(AzureEntraProperties entra) {
        String issuerUri = entra.getIssuerUri();
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withIssuerLocation(issuerUri).build();
        configureDecoder(decoder, entra);
        return decoder;
    }
}
