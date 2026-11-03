package com.milsabores.bff.security;

import com.milsabores.bff.client.UsuarioClient;
import com.milsabores.bff.dto.UsuarioDTO;
import com.milsabores.bff.proxy.DownstreamApiProxy;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @MockitoBean
    private DownstreamApiProxy downstreamApiProxy;

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

    @Test
    void adminSinTokenResponde401() throws Exception {
        mockMvc.perform(post("/api/admin/colas")
                        .contentType("application/json")
                        .content("{\"nombre\":\"prueba.cola\"}"))
                .andExpect(status().isUnauthorized());
        verify(downstreamApiProxy, never()).forward(any(), any());
    }

    @Test
    void adminConTokenMaloResponde401() throws Exception {
        mockMvc.perform(post("/api/admin/colas")
                        .header("Authorization", "Bearer invalido")
                        .contentType("application/json")
                        .content("{\"nombre\":\"prueba.cola\"}"))
                .andExpect(status().isUnauthorized());
        verify(downstreamApiProxy, never()).forward(any(), any());
    }

    @Test
    void adminConRolClienteResponde403() throws Exception {
        mockMvc.perform(post("/api/admin/colas")
                        .header("Authorization", "Bearer " + tokenConRol("CLIENTE"))
                        .contentType("application/json")
                        .content("{\"nombre\":\"prueba.cola\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Se requiere el rol ADMIN"));
        verify(downstreamApiProxy, never()).forward(any(), any());
    }

    @Test
    void adminConRolAdminLlegaAlProxy() throws Exception {
        when(downstreamApiProxy.forward(any(), any())).thenReturn(ResponseEntity.status(201).build());

        mockMvc.perform(post("/api/admin/colas")
                        .header("Authorization", "Bearer " + tokenConRol("ADMIN"))
                        .contentType("application/json")
                        .content("{\"nombre\":\"prueba.cola\"}"))
                .andExpect(status().isCreated());
    }

    private static String tokenConRol(String rol) {
        return Jwts.builder()
                .subject("persona@test.com")
                .claim("roles", rol)
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(testSigningKey())
                .compact();
    }

    private static SecretKey testSigningKey() {
        return Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }
}
