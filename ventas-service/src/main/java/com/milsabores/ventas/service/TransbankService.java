package com.milsabores.ventas.service;

import com.milsabores.ventas.dto.TransbankCommitResponseDTO;
import com.milsabores.ventas.dto.TransbankCreateResponseDTO;

public interface TransbankService {
    
    /**
     * Crear una transacción en Transbank Webpay Plus
     * 
     * @param ventaId ID de la venta
     * @param amount Monto total de la transacción
     * @param returnUrl URL de retorno después del pago
     * @return Respuesta con token y URL de Webpay
     */
    TransbankCreateResponseDTO crearTransaccion(Long ventaId, Double amount, String returnUrl);
    
    /**
     * Confirmar una transacción después del pago
     * 
     * @param token Token de la transacción
     * @return Respuesta con detalles de la transacción
     */
    TransbankCommitResponseDTO confirmarTransaccion(String token);
}
