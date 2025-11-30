package com.milsabores.producto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(hidden = true)
public class Producto {

    @Id
    @Column(name = "code", length = 20)
    private String code; // TC001, TT001, etc.

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200)
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Categoria categoria;

    @Column(name = "tipo_forma", length = 50)
    private String tipoForma; // cuadrada, circular, null

    @ElementCollection
    @CollectionTable(name = "producto_tamanos", joinColumns = @JoinColumn(name = "producto_code"))
    @Column(name = "tamano")
    private List<String> tamanosDisponibles;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio debe ser mayor o igual a 0")
    @Column(name = "precio_clp", nullable = false)
    private Integer precioCLP;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock debe ser mayor o igual a 0")
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "personalizable", nullable = false)
    private Boolean personalizable = false;

    @Column(name = "max_msg_chars")
    private Integer maxMsgChars = 0;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @ElementCollection
    @CollectionTable(name = "producto_etiquetas", joinColumns = @JoinColumn(name = "producto_code"))
    @Column(name = "etiqueta")
    private List<String> etiquetas;

    @Column(name = "imagen", length = 255)
    private String imagen;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
