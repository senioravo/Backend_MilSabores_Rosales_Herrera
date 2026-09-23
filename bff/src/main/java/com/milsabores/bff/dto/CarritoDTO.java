package com.milsabores.bff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarritoDTO {
    private Long usuarioId;
    private List<CarritoItemDTO> items;
    private Integer totalItems;
    private Integer totalPrecio;
}
