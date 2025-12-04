package com.milsabores.ventas.controller;

import com.milsabores.ventas.dto.*;
import com.milsabores.ventas.model.EstadoVenta;
import com.milsabores.ventas.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "API para gestión de ventas")
public class VentaController {
    
    private final VentaService ventaService;
    
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
}
