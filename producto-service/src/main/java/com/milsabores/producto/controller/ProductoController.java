package com.milsabores.producto.controller;

import com.milsabores.producto.dto.ProductoDTO;
import com.milsabores.producto.dto.ProductoResponseDTO;
import com.milsabores.producto.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Productos", description = "API para gestión de productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping("")
    @Operation(summary = "Obtener productos con paginación y filtros", 
               description = "Devuelve una lista paginada de productos con soporte para filtros múltiples")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida correctamente")
    })
    public ResponseEntity<?> obtenerTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoriaId,
            @RequestParam(required = false) Integer minPrecio,
            @RequestParam(required = false) Integer maxPrecio,
            @RequestParam(required = false) Boolean personalizable,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        
        // Si no hay paginación solicitada (legacy)
        if (page == 0 && size == 10 && categoriaId == null && minPrecio == null && 
            maxPrecio == null && personalizable == null && sortBy == null) {
            List<ProductoResponseDTO> productos = productoService.obtenerTodos();
            return ResponseEntity.ok(productos);
        }
        
        // Con paginación y filtros
        var productosPaginados = productoService.obtenerProductosPaginados(
            page, size, categoriaId, minPrecio, maxPrecio, personalizable, sortBy, sortDir
        );
        return ResponseEntity.ok(productosPaginados);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Obtener producto por código", description = "Devuelve un producto específico por su código")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductoResponseDTO> obtenerPorCodigo(@PathVariable String code) {
        ProductoResponseDTO producto = productoService.obtenerPorCodigo(code);
        return ResponseEntity.ok(producto);
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Obtener productos por categoría", description = "Devuelve todos los productos de una categoría específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida correctamente")
    })
    public ResponseEntity<List<ProductoResponseDTO>> obtenerPorCategoria(@PathVariable String categoriaId) {
        List<ProductoResponseDTO> productos = productoService.obtenerPorCategoria(categoriaId);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/destacados")
    @Operation(summary = "Obtener productos destacados", description = "Devuelve los productos destacados con stock disponible")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos destacados obtenida correctamente")
    })
    public ResponseEntity<List<ProductoResponseDTO>> obtenerDestacados(
            @RequestParam(required = false) Integer limit) {
        List<ProductoResponseDTO> productos = productoService.obtenerDestacados(limit);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar productos por nombre", description = "Busca productos que coincidan con el nombre proporcionado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente")
    })
    public ResponseEntity<List<ProductoResponseDTO>> buscarPorNombre(@RequestParam String nombre) {
        List<ProductoResponseDTO> productos = productoService.buscarPorNombre(nombre);
        return ResponseEntity.ok(productos);
    }

    @PostMapping("")
    @Operation(summary = "Crear nuevo producto", description = "Crea un nuevo producto en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoDTO productoDTO) {
        ProductoResponseDTO producto = productoService.crear(productoDTO);
        return new ResponseEntity<>(producto, HttpStatus.CREATED);
    }

    @PutMapping("/{code}")
    @Operation(summary = "Actualizar producto", description = "Actualiza los datos de un producto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable String code,
            @Valid @RequestBody ProductoDTO productoDTO) {
        ProductoResponseDTO producto = productoService.actualizar(code, productoDTO);
        return ResponseEntity.ok(producto);
    }

    @PatchMapping("/{code}/stock")
    @Operation(summary = "Actualizar stock del producto", description = "Actualiza únicamente el stock de un producto (útil para ventas)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "400", description = "Stock inválido (negativo)")
    })
    public ResponseEntity<ProductoResponseDTO> actualizarStock(
            @PathVariable String code,
            @RequestParam Integer stock) {
        ProductoResponseDTO producto = productoService.actualizarStock(code, stock);
        return ResponseEntity.ok(producto);
    }
    
    @PatchMapping("/{code}/reducir-stock")
    @Operation(summary = "Reducir stock al vender", description = "Reduce el stock de un producto cuando es vendido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock reducido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "400", description = "Stock insuficiente")
    })
    public ResponseEntity<ProductoResponseDTO> reducirStock(
            @PathVariable String code,
            @RequestParam Integer cantidad) {
        ProductoResponseDTO producto = productoService.reducirStock(code, cantidad);
        return ResponseEntity.ok(producto);
    }

    @DeleteMapping("/{code}")
    @Operation(summary = "Eliminar producto", description = "Desactiva un producto del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable String code) {
        productoService.eliminar(code);
        return ResponseEntity.noContent().build();
    }
}
