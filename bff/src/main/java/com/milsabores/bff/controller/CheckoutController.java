package com.milsabores.bff.controller;

import com.milsabores.bff.dto.CheckoutResultDTO;
import com.milsabores.bff.dto.CheckoutResumenDTO;
import com.milsabores.bff.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Tras Spring Security en el BFF, {@code /bff/**} exige JWT válido; {@code X-User-Id}
 * sigue inyectado por el api-gateway para la orquestación checkout.
 */
@RestController
@RequestMapping("/bff/checkout")
@RequiredArgsConstructor
@Tag(name = "BFF Checkout", description = "Orquestacion de carrito -> venta -> pago Transbank para el frontend web")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @GetMapping("/resumen")
    @Operation(summary = "Resumen del carrito con precio/stock verificados contra producto-service")
    public ResponseEntity<CheckoutResumenDTO> resumen(@RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.ok(checkoutService.obtenerResumen(usuarioId));
    }

    @PostMapping
    @Operation(summary = "Crea la venta a partir del carrito actual e inicia el pago con Transbank")
    public ResponseEntity<CheckoutResultDTO> checkout(@RequestHeader("X-User-Id") Long usuarioId) {
        return ResponseEntity.ok(checkoutService.procesarCheckout(usuarioId));
    }
}
