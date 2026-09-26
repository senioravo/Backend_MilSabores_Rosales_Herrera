package com.milsabores.bff.controller;

import com.milsabores.bff.client.UsuarioClient;
import com.milsabores.bff.dto.UsuarioDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * EP1 B5: expone /bff/me reenviando identidad Entra al usuario-service (sync Neon).
 */
@RestController
@RequestMapping("/bff")
public class IdentityController {

    private final UsuarioClient usuarioClient;

    public IdentityController(UsuarioClient usuarioClient) {
        this.usuarioClient = usuarioClient;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> me(
            @RequestHeader(value = "X-Auth-Provider", required = false) String authProvider,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-Entra-Oid", required = false) String entraOid,
            @RequestHeader(value = "X-User-Name", required = false) String userName,
            @RequestHeader(value = "X-User-Id", required = false) String legacyUserId) {

        UsuarioDTO perfil = usuarioClient.obtenerPerfilMe(
                authProvider, userEmail, entraOid, userName, legacyUserId);
        return ResponseEntity.ok(perfil);
    }
}
