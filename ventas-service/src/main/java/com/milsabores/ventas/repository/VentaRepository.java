package com.milsabores.ventas.repository;

import com.milsabores.ventas.model.Venta;
import com.milsabores.ventas.model.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    
    List<Venta> findByUsuarioId(Long usuarioId);
    
    List<Venta> findByEstado(EstadoVenta estado);
    
    List<Venta> findByUsuarioIdAndEstado(Long usuarioId, EstadoVenta estado);
    
    Optional<Venta> findByTransbankToken(String token);
    
    @Query("SELECT v FROM Venta v WHERE v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<Venta> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.estado = :estado")
    Long getTotalVentasByEstado(EstadoVenta estado);
}
