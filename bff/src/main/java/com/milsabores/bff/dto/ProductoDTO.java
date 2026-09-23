package com.milsabores.bff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoDTO {
    private String code;
    private String nombre;
    private Integer precioCLP;
    private Integer stock;
    private Boolean activo;
}
