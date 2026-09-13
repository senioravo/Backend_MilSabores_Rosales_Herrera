package com.milsabores.carrito.controller;

import com.milsabores.carrito.dto.AgregarItemDTO;
import com.milsabores.carrito.dto.CarritoItemDTO;
import com.milsabores.carrito.dto.CarritoResponseDTO;
import com.milsabores.carrito.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carritos")
@Tag(name = "Carrito", description = "API para gestión del carrito de compras")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener carrito de usuario", description = "Devuelve el carrito completo de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito obtenido correctamente")
    })
    public ResponseEntity<CarritoResponseDTO> obtenerCarrito(@PathVariable Long usuarioId) {
        CarritoResponseDTO carrito = carritoService.obtenerCarrito(usuarioId);
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/agregar")
    @Operation(summary = "Agregar producto al carrito", description = "Agrega un producto al carrito o actualiza su cantidad si ya existe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto agregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<CarritoItemDTO> agregarItem(@Valid @RequestBody AgregarItemDTO agregarDTO) {
        CarritoItemDTO item = carritoService.agregarItem(agregarDTO);
        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }

    @PutMapping("/item/{itemId}")
    @Operation(summary = "Actualizar cantidad de item", description = "Actualiza la cantidad de un item en el carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Item no encontrado")
    })
    public ResponseEntity<CarritoItemDTO> actualizarCantidad(
            @PathVariable Long itemId,
            @RequestParam Integer cantidad) {
        CarritoItemDTO item = carritoService.actualizarCantidad(itemId, cantidad);
        if (item == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/item/{itemId}")
    @Operation(summary = "Eliminar item del carrito", description = "Elimina un item específico del carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Item no encontrado")
    })
    public ResponseEntity<Void> eliminarItem(@PathVariable Long itemId) {
        carritoService.eliminarItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuario/{usuarioId}/producto/{productoCode}")
    @Operation(summary = "Eliminar producto del carrito", description = "Elimina un producto específico del carrito de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente")
    })
    public ResponseEntity<Void> eliminarItemPorProducto(
            @PathVariable Long usuarioId,
            @PathVariable String productoCode) {
        carritoService.eliminarItemPorProducto(usuarioId, productoCode);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuario/{usuarioId}")
    @Operation(summary = "Limpiar carrito", description = "Elimina todos los items del carrito de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Carrito limpiado exitosamente")
    })
    public ResponseEntity<Void> limpiarCarrito(@PathVariable Long usuarioId) {
        carritoService.limpiarCarrito(usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{usuarioId}/total")
    @Operation(summary = "Obtener total del carrito", description = "Devuelve el precio total del carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total obtenido correctamente")
    })
    public ResponseEntity<Integer> obtenerTotal(@PathVariable Long usuarioId) {
        Integer total = carritoService.obtenerTotal(usuarioId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/usuario/{usuarioId}/cantidad")
    @Operation(summary = "Obtener cantidad de items", description = "Devuelve la cantidad total de items en el carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad obtenida correctamente")
    })
    public ResponseEntity<Integer> obtenerCantidadItems(@PathVariable Long usuarioId) {
        Integer cantidad = carritoService.obtenerCantidadItems(usuarioId);
        return ResponseEntity.ok(cantidad);
    }
}
