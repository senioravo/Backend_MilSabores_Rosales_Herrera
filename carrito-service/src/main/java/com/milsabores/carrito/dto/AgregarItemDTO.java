package com.milsabores.carrito.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgregarItemDTO {

    @NotNull(message = "El ID de usuario es obligatorio")
    private Long usuarioId;

    @NotBlank(message = "El código de producto es obligatorio")
    private String productoCode;

    @NotBlank(message = "El nombre de producto es obligatorio")
    private String productoNombre;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio debe ser mayor o igual a 0")
    private Integer precioCLP;

    private String productoImagen;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    private Integer stockDisponible;
}
