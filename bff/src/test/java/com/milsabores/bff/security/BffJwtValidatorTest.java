package com.milsabores.bff.security;

import com.milsabores.bff.config.AzureEntraProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Prueba la segunda validación JWT sin levantar Spring: misma lógica que el gateway (HS256 legacy).
 */
class BffJwtValidatorTest {

    private static final String SECRET = "test-jwt-secret-milsabores-bff-security-min-32-chars!!";

    private BffJwtValidator validator;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        AzureEntraProperties entra = new AzureEntraProperties();
        entra.setEnabled(false);
        validator = new BffJwtValidator(entra, null);
        setSecret(validator, SECRET);
    }

    @Test
    void validLegacyJwtPassesSecondValidation() {
        String token = legacyToken("cliente@test.com", new Date(System.currentTimeMillis() + 60_000));
        HttpServletRequest request = mock(HttpServletRequest.class);

        validator.validateAndAuthenticate(request, token);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isInstanceOf(BffAuthenticationToken.class);
        assertThat(auth.getName()).isEqualTo("cliente@test.com");
        assertThat(((BffAuthenticationToken) auth).getAuthProvider()).isEqualTo("legacy");
    }

    @Test
    void expiredLegacyJwtFailsSecondValidation() {
        String token = legacyToken("cliente@test.com", new Date(System.currentTimeMillis() - 1_000));
        HttpServletRequest request = mock(HttpServletRequest.class);

        assertThatThrownBy(() -> validator.validateAndAuthenticate(request, token))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void tokenSignedWithWrongSecretFailsSecondValidation() {
        String token = Jwts.builder()
                .subject("otro@test.com")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey("otro-secreto-distinto-minimo-32-chars!!"))
                .compact();
        HttpServletRequest request = mock(HttpServletRequest.class);

        assertThatThrownBy(() -> validator.validateAndAuthenticate(request, token))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    private static String legacyToken(String subject, Date expiration) {
        return Jwts.builder()
                .subject(subject)
                .expiration(expiration)
                .signWith(signingKey(SECRET))
                .compact();
    }

    private static SecretKey signingKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static void setSecret(BffJwtValidator target, String secret) {
        try {
            var field = BffJwtValidator.class.getDeclaredField("jwtSecret");
            field.setAccessible(true);
            field.set(target, secret);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
