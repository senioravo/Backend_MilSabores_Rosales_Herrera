package com.milsabores.bff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarritoItemDTO {
    private String productoCode;
    private String productoNombre;
    private Integer precioCLP;
    private String productoImagen;
    private Integer cantidad;
    private Integer subtotal;
}
