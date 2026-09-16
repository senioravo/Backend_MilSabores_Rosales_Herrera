package com.milsabores.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequestDTO {
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;
    private List<DetalleVentaDTO> detalles;
    private Integer subtotal;
    private Integer iva;
    private Integer total;
}
