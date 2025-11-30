package com.milsabores.carrito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoResponseDTO {
    private Long usuarioId;
    private List<CarritoItemDTO> items;
    private Integer totalItems;
    private Integer totalPrecio;
}
