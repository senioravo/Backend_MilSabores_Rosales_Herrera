package com.milsabores.ventas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransbankResponseDTO {
    
    private boolean exitoso;
    private String mensaje;
    private String token;
    private String url; // URL de redirección a Webpay
    private String ordenCompra;
    private Double monto;
    private String codigoAutorizacion;
    private String numeroTarjeta;
    private String fechaTransaccion;
    private Integer responseCode; // Código de respuesta de Transbank
}
