package com.milsabores.ventas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaDTO {
    
    @NotBlank(message = "El código del producto es requerido")
    private String productoCode;
    
    @NotBlank(message = "El nombre del producto es requerido")
    private String productoNombre;
    
    private String productoImagen;
    
    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;
    
    @NotNull(message = "El precio unitario es requerido")
    @Positive(message = "El precio unitario debe ser mayor a cero")
    private Integer precioUnitario;
    
    private Integer subtotal;
}
