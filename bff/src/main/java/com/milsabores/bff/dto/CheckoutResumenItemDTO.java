package com.milsabores.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResumenItemDTO {
    private String productoCode;
    private String productoNombre;
    private String productoImagen;
    private Integer cantidad;
    private Integer precioUnitarioActual;
    private Integer subtotal;
    private boolean precioDesactualizado;
    private boolean stockInsuficiente;
}
