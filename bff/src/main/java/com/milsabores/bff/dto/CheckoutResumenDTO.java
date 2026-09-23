package com.milsabores.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResumenDTO {
    private Long usuarioId;
    private List<CheckoutResumenItemDTO> items;
    private Integer subtotal;
    private Integer iva;
    private Integer total;
    private boolean listoParaPagar;
}
