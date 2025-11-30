package com.milsabores.ventas.service;

import com.milsabores.ventas.dto.*;
import com.milsabores.ventas.model.EstadoVenta;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaService {
    
    VentaResponseDTO crearVenta(VentaRequestDTO ventaRequest);
    
    VentaResponseDTO obtenerVentaPorId(Long id);
    
    List<VentaResponseDTO> obtenerTodasLasVentas();
    
    List<VentaResponseDTO> obtenerVentasPorUsuario(Long usuarioId);
    
    List<VentaResponseDTO> obtenerVentasPorEstado(EstadoVenta estado);
    
    VentaResponseDTO actualizarEstadoVenta(Long id, EstadoVenta nuevoEstado);
    
    TransbankResponseDTO procesarPagoTransbank(Long ventaId);
    
    VentaResponseDTO confirmarPago(Long ventaId, String token, boolean exitoso);
    
    void eliminarVenta(Long id);
    
    List<VentaResponseDTO> obtenerVentasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
