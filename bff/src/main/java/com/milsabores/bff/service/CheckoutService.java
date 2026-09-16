package com.milsabores.bff.service;

import com.milsabores.bff.dto.CheckoutResultDTO;
import com.milsabores.bff.dto.CheckoutResumenDTO;

public interface CheckoutService {

    CheckoutResumenDTO obtenerResumen(Long usuarioId);

    CheckoutResultDTO procesarCheckout(Long usuarioId);
}
