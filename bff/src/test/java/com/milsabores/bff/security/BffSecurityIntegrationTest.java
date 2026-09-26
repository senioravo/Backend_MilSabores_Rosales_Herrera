package com.milsabores.bff.security;

import com.milsabores.bff.client.UsuarioClient;
import com.milsabores.bff.dto.UsuarioDTO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BffSecurityIntegrationTest {

    private static final String TEST_SECRET = "test-jwt-secret-milsabores-bff-security-min-32-chars!!";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioClient usuarioClient;

    @Test
    void bffMeWithoutBearerReturns401() throws Exception {
        mockMvc.perform(get("/bff/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void bffMeWithInvalidBearerReturns401() throws Exception {
        mockMvc.perform(get("/bff/me").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bffMeWithFakeIdentityHeadersButNoBearerStill401() throws Exception {
        mockMvc.perform(get("/bff/me")
                        .header("X-User-Email", "admin@fake.com")
                        .header("X-Entra-Oid", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bffMeWithValidLegacyJwtPassesSecondValidation() throws Exception {
        when(usuarioClient.obtenerPerfilMe(any(), any(), any(), any(), any()))
                .thenReturn(new UsuarioDTO(1L, "Cliente", "cliente@test.com"));

        String token = Jwts.builder()
                .subject("cliente@test.com")
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(testSigningKey())
                .compact();

        mockMvc.perform(get("/bff/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("cliente@test.com"));
    }

    @Test
    void actuatorHealthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    private static SecretKey testSigningKey() {
        return Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }
}
