package com.milsabores.ventas.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "detalle_ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVenta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    @JsonBackReference
    private Venta venta;
    
    @Column(name = "producto_code", nullable = false)
    private String productoCode;
    
    @Column(name = "producto_nombre", nullable = false)
    private String productoNombre;
    
    @Column(name = "producto_imagen")
    private String productoImagen;
    
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
    
    @Column(name = "precio_unitario", nullable = false)
    private Integer precioUnitario;
    
    @Column(name = "subtotal", nullable = false)
    private Integer subtotal;
    
    @PrePersist
    @PreUpdate
    public void calcularSubtotal() {
        this.subtotal = this.cantidad * this.precioUnitario;
    }
}
