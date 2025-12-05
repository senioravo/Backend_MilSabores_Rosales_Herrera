package com.milsabores.ventas.controller;

import com.milsabores.ventas.dto.*;
import com.milsabores.ventas.model.EstadoVenta;
import com.milsabores.ventas.model.Venta;
import com.milsabores.ventas.repository.VentaRepository;
import com.milsabores.ventas.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "API para gestión de ventas")
public class VentaController {
    
    private final VentaService ventaService;
    private final VentaRepository ventaRepository;
    
    @PostMapping
    @Operation(summary = "Crear una nueva venta")
    public ResponseEntity<VentaResponseDTO> crearVenta(@Valid @RequestBody VentaRequestDTO ventaRequest) {
        VentaResponseDTO venta = ventaService.crearVenta(ventaRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una venta por ID")
    public ResponseEntity<VentaResponseDTO> obtenerVentaPorId(@PathVariable Long id) {
        VentaResponseDTO venta = ventaService.obtenerVentaPorId(id);
        return ResponseEntity.ok(venta);
    }
    
    @GetMapping
    @Operation(summary = "Obtener todas las ventas")
    public ResponseEntity<List<VentaResponseDTO>> obtenerTodasLasVentas() {
        List<VentaResponseDTO> ventas = ventaService.obtenerTodasLasVentas();
        return ResponseEntity.ok(ventas);
    }
    
    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener ventas por usuario")
    public ResponseEntity<List<VentaResponseDTO>> obtenerVentasPorUsuario(@PathVariable Long usuarioId) {
        List<VentaResponseDTO> ventas = ventaService.obtenerVentasPorUsuario(usuarioId);
        return ResponseEntity.ok(ventas);
    }
    
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener ventas por estado")
    public ResponseEntity<List<VentaResponseDTO>> obtenerVentasPorEstado(@PathVariable EstadoVenta estado) {
        List<VentaResponseDTO> ventas = ventaService.obtenerVentasPorEstado(estado);
        return ResponseEntity.ok(ventas);
    }
    
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de una venta")
    public ResponseEntity<VentaResponseDTO> actualizarEstadoVenta(
            @PathVariable Long id,
            @RequestParam EstadoVenta estado) {
        VentaResponseDTO venta = ventaService.actualizarEstadoVenta(id, estado);
        return ResponseEntity.ok(venta);
    }
    
    @PostMapping("/{id}/pagar")
    @Operation(summary = "Procesar pago con Transbank")
    public ResponseEntity<TransbankResponseDTO> procesarPago(@PathVariable Long id) {
        TransbankResponseDTO response = ventaService.procesarPagoTransbank(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/confirmar-pago")
    @Operation(summary = "Confirmar resultado del pago")
    public ResponseEntity<VentaResponseDTO> confirmarPago(
            @PathVariable Long id,
            @RequestParam String token,
            @RequestParam boolean exitoso) {
        VentaResponseDTO venta = ventaService.confirmarPago(id, token, exitoso);
        return ResponseEntity.ok(venta);
    }
    
    @GetMapping("/fecha")
    @Operation(summary = "Obtener ventas por rango de fechas")
    public ResponseEntity<List<VentaResponseDTO>> obtenerVentasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<VentaResponseDTO> ventas = ventaService.obtenerVentasPorFecha(fechaInicio, fechaFin);
        return ResponseEntity.ok(ventas);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una venta")
    public ResponseEntity<Void> eliminarVenta(@PathVariable Long id) {
        ventaService.eliminarVenta(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/transbank/return")
    @Operation(summary = "Endpoint de retorno de Transbank después del pago")
    public ResponseEntity<Void> handleTransbankReturn(@RequestParam("token_ws") String token) {
        try {
            log.info("Retorno de Transbank con token: {}", token);
            
            // Buscar venta por token
            Venta venta = ventaRepository.findByTransbankToken(token)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con token: " + token));
            
            // Confirmar pago con Transbank
            VentaResponseDTO ventaConfirmada = ventaService.confirmarPago(venta.getId(), token, true);
            
            // Redirigir al frontend con resultado
            String frontendUrl = "https://dsy-1104-rosales-herrera.vercel.app/checkout/result?ventaId=" 
                + venta.getId() + "&status=" + ventaConfirmada.getEstado();
            
            log.info("Redirigiendo a frontend: {}", frontendUrl);
            
            return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", frontendUrl)
                .build();
            
        } catch (Exception e) {
            log.error("Error en retorno de Transbank", e);
            return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "https://dsy-1104-rosales-herrera.vercel.app/checkout/error")
                .build();
        }
    }
}
