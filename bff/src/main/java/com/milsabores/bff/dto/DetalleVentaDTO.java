package com.milsabores.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Debe coincidir con ventas-service.dto.DetalleVentaDTO: productoNombre y
 * precioUnitario son @NotBlank/@NotNull en ese servicio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaDTO {
    private String productoCode;
    private String productoNombre;
    private String productoImagen;
    private Integer cantidad;
    private Integer precioUnitario;
    private Integer subtotal;
}
