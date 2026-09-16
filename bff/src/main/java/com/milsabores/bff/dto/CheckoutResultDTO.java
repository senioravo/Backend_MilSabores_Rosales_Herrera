package com.milsabores.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResultDTO {
    private Long ventaId;
    private String estado;
    private Integer subtotal;
    private Integer iva;
    private Integer total;
    private String transbankToken;
    private String transbankUrl;
    private String transbankOrdenCompra;
}
