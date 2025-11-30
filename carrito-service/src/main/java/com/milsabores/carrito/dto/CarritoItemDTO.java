package com.milsabores.carrito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemDTO {
    private Long id;
    private Long usuarioId;
    private String productoCode;
    private String productoNombre;
    private Integer precioCLP;
    private String productoImagen;
    private Integer cantidad;
    private Integer stockDisponible;
    private LocalDateTime fechaAgregado;
    private Integer subtotal;
}
