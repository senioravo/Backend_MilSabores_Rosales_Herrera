package com.milsabores.producto.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {
    private String code;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    @NotBlank(message = "La categoría es obligatoria")
    private String categoriaId;
    
    private String tipoForma;
    private List<String> tamanosDisponibles;
    
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio debe ser mayor o igual a 0")
    private Integer precioCLP;
    
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock debe ser mayor o igual a 0")
    private Integer stock;
    
    private Boolean personalizable;
    private Integer maxMsgChars;
    private String descripcion;
    private List<String> etiquetas;
    private String imagen;
    private Boolean activo;
}
