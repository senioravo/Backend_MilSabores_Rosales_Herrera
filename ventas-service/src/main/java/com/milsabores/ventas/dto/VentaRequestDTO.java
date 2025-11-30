package com.milsabores.ventas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequestDTO {
    
    @NotNull(message = "El ID del usuario es requerido")
    private Long usuarioId;
    
    @NotBlank(message = "El nombre del usuario es requerido")
    private String usuarioNombre;
    
    @NotBlank(message = "El email del usuario es requerido")
    @Email(message = "El email debe ser válido")
    private String usuarioEmail;
    
    @NotNull(message = "Los detalles de la venta son requeridos")
    @Size(min = 1, message = "Debe haber al menos un producto en la venta")
    private List<DetalleVentaDTO> detalles;
    
    @NotNull(message = "El subtotal es requerido")
    @Positive(message = "El subtotal debe ser mayor a cero")
    private Integer subtotal;
    
    @NotNull(message = "El IVA es requerido")
    @PositiveOrZero(message = "El IVA debe ser mayor o igual a cero")
    private Integer iva;
    
    @NotNull(message = "El total es requerido")
    @Positive(message = "El total debe ser mayor a cero")
    private Integer total;
}
