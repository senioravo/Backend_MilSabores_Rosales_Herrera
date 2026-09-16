package com.milsabores.bff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransbankResponseDTO {
    private boolean exitoso;
    private String mensaje;
    private String token;
    private String url;
    private String ordenCompra;
}
