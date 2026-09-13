package com.milsabores.carrito.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "carrito_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID de usuario es obligatorio")
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @NotNull(message = "El código de producto es obligatorio")
    @Column(name = "producto_code", nullable = false, length = 20)
    private String productoCode;

    @Column(name = "producto_nombre", nullable = false, length = 200)
    private String productoNombre;

    @NotNull(message = "El precio es obligatorio")
    @Column(name = "precio_clp", nullable = false)
    private Integer precioCLP;

    @Column(name = "producto_imagen", length = 255)
    private String productoImagen;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "stock_disponible")
    private Integer stockDisponible;

    @Column(name = "fecha_agregado", nullable = false, updatable = false)
    private LocalDateTime fechaAgregado;

    @PrePersist
    protected void onCreate() {
        this.fechaAgregado = LocalDateTime.now();
    }

    public Integer getSubtotal() {
        return this.precioCLP * this.cantidad;
    }
}
