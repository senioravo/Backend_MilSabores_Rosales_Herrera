package com.milsabores.ventas.dto;

import com.milsabores.ventas.model.EstadoVenta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponseDTO {
    
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;
    private Integer subtotal;
    private Integer iva;
    private Integer total;
    private EstadoVenta estado;
    private String transbankToken;
    private String transbankOrderId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private List<DetalleVentaDTO> detalles;
}
