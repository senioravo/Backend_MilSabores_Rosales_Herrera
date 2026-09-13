package com.milsabores.producto.repository;

import com.milsabores.producto.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String>, JpaSpecificationExecutor<Producto> {
    
    List<Producto> findByCategoriaId(String categoriaId);
    
    List<Producto> findByActivoTrue();
    
    Page<Producto> findByActivoTrue(Pageable pageable);
    
    List<Producto> findByCategoriaIdAndActivoTrue(String categoriaId);
    
    @Query("SELECT p FROM Producto p WHERE p.stock > 0 AND p.activo = true ORDER BY p.precioCLP DESC")
    List<Producto> findFeaturedProducts();
    
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND p.activo = true")
    List<Producto> buscarPorNombre(@Param("nombre") String nombre);
}
