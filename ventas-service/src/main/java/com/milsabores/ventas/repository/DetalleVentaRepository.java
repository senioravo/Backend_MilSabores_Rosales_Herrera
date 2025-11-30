package com.milsabores.ventas.repository;

import com.milsabores.ventas.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    
    List<DetalleVenta> findByVentaId(Long ventaId);
    
    @Query("SELECT d FROM DetalleVenta d WHERE d.productoCode = :productoCode")
    List<DetalleVenta> findByProductoCode(String productoCode);
    
    @Query("SELECT d.productoCode, SUM(d.cantidad) FROM DetalleVenta d GROUP BY d.productoCode ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findProductosMasVendidos();
}
