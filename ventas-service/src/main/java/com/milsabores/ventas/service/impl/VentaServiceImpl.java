package com.milsabores.ventas.service.impl;

import com.milsabores.ventas.dto.*;
import com.milsabores.ventas.exception.VentaNotFoundException;
import com.milsabores.ventas.model.DetalleVenta;
import com.milsabores.ventas.model.EstadoVenta;
import com.milsabores.ventas.model.Venta;
import com.milsabores.ventas.repository.VentaRepository;
import com.milsabores.ventas.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {
    
    private final VentaRepository ventaRepository;
    
    @Override
    @Transactional
    public VentaResponseDTO crearVenta(VentaRequestDTO ventaRequest) {
        // Crear la entidad Venta
        Venta venta = new Venta();
        venta.setUsuarioId(ventaRequest.getUsuarioId());
        venta.setUsuarioNombre(ventaRequest.getUsuarioNombre());
        venta.setUsuarioEmail(ventaRequest.getUsuarioEmail());
        venta.setSubtotal(ventaRequest.getSubtotal());
        venta.setIva(ventaRequest.getIva());
        venta.setTotal(ventaRequest.getTotal());
        venta.setEstado(EstadoVenta.PENDIENTE);
        venta.setFechaCreacion(LocalDateTime.now());
        
        // Agregar detalles
        for (DetalleVentaDTO detalleDTO : ventaRequest.getDetalles()) {
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProductoCode(detalleDTO.getProductoCode());
            detalle.setProductoNombre(detalleDTO.getProductoNombre());
            detalle.setProductoImagen(detalleDTO.getProductoImagen());
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
            detalle.calcularSubtotal();
            venta.addDetalle(detalle);
        }
        
        // Guardar
        Venta ventaGuardada = ventaRepository.save(venta);
        
        return convertirADTO(ventaGuardada);
    }
    
    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO obtenerVentaPorId(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new VentaNotFoundException("Venta no encontrada con ID: " + id));
        return convertirADTO(venta);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> obtenerTodasLasVentas() {
        return ventaRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> obtenerVentasPorUsuario(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> obtenerVentasPorEstado(EstadoVenta estado) {
        return ventaRepository.findByEstado(estado).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public VentaResponseDTO actualizarEstadoVenta(Long id, EstadoVenta nuevoEstado) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new VentaNotFoundException("Venta no encontrada con ID: " + id));
        
        venta.setEstado(nuevoEstado);
        venta.setFechaActualizacion(LocalDateTime.now());
        
        Venta ventaActualizada = ventaRepository.save(venta);
        return convertirADTO(ventaActualizada);
    }
    
    @Override
    @Transactional
    public TransbankResponseDTO procesarPagoTransbank(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new VentaNotFoundException("Venta no encontrada con ID: " + ventaId));
        
        // Simular integración con Transbank
        // En producción, aquí se haría la llamada real a la API de Transbank
        String token = "TBK_" + UUID.randomUUID().toString();
        String ordenCompra = "OC_" + ventaId + "_" + System.currentTimeMillis();
        
        venta.setTransbankToken(token);
        venta.setTransbankOrderId(ordenCompra);
        venta.setEstado(EstadoVenta.PROCESANDO);
        venta.setFechaActualizacion(LocalDateTime.now());
        
        ventaRepository.save(venta);
        
        // Simular respuesta exitosa de Transbank (80% de éxito)
        boolean exitoso = Math.random() < 0.8;
        
        TransbankResponseDTO response = new TransbankResponseDTO();
        response.setExitoso(exitoso);
        response.setToken(token);
        response.setOrdenCompra(ordenCompra);
        response.setMonto(venta.getTotal());
        response.setFechaTransaccion(LocalDateTime.now().toString());
        
        if (exitoso) {
            response.setMensaje("Transacción aprobada");
            response.setCodigoAutorizacion("AUTH_" + (int)(Math.random() * 1000000));
            response.setNumeroTarjeta("**** **** **** " + (int)(Math.random() * 10000));
        } else {
            response.setMensaje("Transacción rechazada - Fondos insuficientes");
        }
        
        return response;
    }
    
    @Override
    @Transactional
    public VentaResponseDTO confirmarPago(Long ventaId, String token, boolean exitoso) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new VentaNotFoundException("Venta no encontrada con ID: " + ventaId));
        
        if (exitoso) {
            venta.setEstado(EstadoVenta.COMPLETADA);
        } else {
            venta.setEstado(EstadoVenta.RECHAZADA);
        }
        
        venta.setFechaActualizacion(LocalDateTime.now());
        Venta ventaActualizada = ventaRepository.save(venta);
        
        return convertirADTO(ventaActualizada);
    }
    
    @Override
    @Transactional
    public void eliminarVenta(Long id) {
        if (!ventaRepository.existsById(id)) {
            throw new VentaNotFoundException("Venta no encontrada con ID: " + id);
        }
        ventaRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> obtenerVentasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.findByFechaCreacionBetween(fechaInicio, fechaFin).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    private VentaResponseDTO convertirADTO(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setId(venta.getId());
        dto.setUsuarioId(venta.getUsuarioId());
        dto.setUsuarioNombre(venta.getUsuarioNombre());
        dto.setUsuarioEmail(venta.getUsuarioEmail());
        dto.setSubtotal(venta.getSubtotal());
        dto.setIva(venta.getIva());
        dto.setTotal(venta.getTotal());
        dto.setEstado(venta.getEstado());
        dto.setTransbankToken(venta.getTransbankToken());
        dto.setTransbankOrderId(venta.getTransbankOrderId());
        dto.setFechaCreacion(venta.getFechaCreacion());
        dto.setFechaActualizacion(venta.getFechaActualizacion());
        
        List<DetalleVentaDTO> detallesDTO = venta.getDetalles().stream()
                .map(this::convertirDetalleADTO)
                .collect(Collectors.toList());
        dto.setDetalles(detallesDTO);
        
        return dto;
    }
    
    private DetalleVentaDTO convertirDetalleADTO(DetalleVenta detalle) {
        DetalleVentaDTO dto = new DetalleVentaDTO();
        dto.setProductoCode(detalle.getProductoCode());
        dto.setProductoNombre(detalle.getProductoNombre());
        dto.setProductoImagen(detalle.getProductoImagen());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }
}
