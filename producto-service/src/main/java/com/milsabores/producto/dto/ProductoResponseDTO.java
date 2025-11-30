package com.milsabores.producto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {
    private String code;
    private String nombre;
    private CategoriaDTO categoria;
    private String tipoForma;
    private List<String> tamanosDisponibles;
    private Integer precioCLP;
    private Integer stock;
    private Boolean personalizable;
    private Integer maxMsgChars;
    private String descripcion;
    private List<String> etiquetas;
    private String imagen;
    private Boolean activo;
}
