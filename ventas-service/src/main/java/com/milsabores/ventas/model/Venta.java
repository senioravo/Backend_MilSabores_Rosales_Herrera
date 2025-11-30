package com.milsabores.ventas.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    
    @Column(name = "usuario_nombre")
    private String usuarioNombre;
    
    @Column(name = "usuario_email")
    private String usuarioEmail;
    
    @Column(name = "subtotal", nullable = false)
    private Integer subtotal;
    
    @Column(name = "iva", nullable = false)
    private Integer iva;
    
    @Column(name = "total", nullable = false)
    private Integer total;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoVenta estado = EstadoVenta.PENDIENTE;
    
    @Column(name = "transbank_token")
    private String transbankToken;
    
    @Column(name = "transbank_order_id")
    private String transbankOrderId;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DetalleVenta> detalles = new ArrayList<>();
    
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public void addDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }
    
    public void removeDetalle(DetalleVenta detalle) {
        detalles.remove(detalle);
        detalle.setVenta(null);
    }
}
