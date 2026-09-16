package com.milsabores.bff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VentaResponseDTO {
    private Long id;
    private String estado;
    private Integer subtotal;
    private Integer iva;
    private Integer total;
}
