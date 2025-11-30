package com.milsabores.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private boolean success;
    private String message;
    private UsuarioResponseDTO user;
    private String token; // Para futuro uso con JWT
}
