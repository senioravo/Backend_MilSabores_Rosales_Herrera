package com.milsabores.producto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {
    private String id;
    private String nombre;
    private String descripcion;
    private String imagen;
}
