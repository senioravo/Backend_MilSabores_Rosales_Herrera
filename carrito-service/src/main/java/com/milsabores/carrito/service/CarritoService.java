package com.milsabores.carrito.service;

import com.milsabores.carrito.dto.AgregarItemDTO;
import com.milsabores.carrito.dto.CarritoItemDTO;
import com.milsabores.carrito.dto.CarritoResponseDTO;
import com.milsabores.carrito.exception.CarritoItemNotFoundException;
import com.milsabores.carrito.model.CarritoItem;
import com.milsabores.carrito.repository.CarritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    /**
     * Obtiene el carrito completo de un usuario
     */
    @Transactional(readOnly = true)
    public CarritoResponseDTO obtenerCarrito(Long usuarioId) {
        List<CarritoItem> items = carritoRepository.findByUsuarioId(usuarioId);
        
        List<CarritoItemDTO> itemsDTO = items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Integer totalItems = items.stream()
                .mapToInt(CarritoItem::getCantidad)
                .sum();

        Integer totalPrecio = items.stream()
                .mapToInt(CarritoItem::getSubtotal)
                .sum();

        return new CarritoResponseDTO(usuarioId, itemsDTO, totalItems, totalPrecio);
    }

    /**
     * Agrega un producto al carrito
     */
    public CarritoItemDTO agregarItem(AgregarItemDTO agregarDTO) {
        // Verificar si el producto ya existe en el carrito del usuario
        Optional<CarritoItem> existingItem = carritoRepository
                .findByUsuarioIdAndProductoCode(agregarDTO.getUsuarioId(), agregarDTO.getProductoCode());

        CarritoItem item;
        if (existingItem.isPresent()) {
            // Si existe, actualizar cantidad
            item = existingItem.get();
            item.setCantidad(item.getCantidad() + agregarDTO.getCantidad());
            item.setPrecioCLP(agregarDTO.getPrecioCLP()); // Actualizar precio por si cambió
            item.setStockDisponible(agregarDTO.getStockDisponible());
        } else {
            // Si no existe, crear nuevo item
            item = new CarritoItem();
            item.setUsuarioId(agregarDTO.getUsuarioId());
            item.setProductoCode(agregarDTO.getProductoCode());
            item.setProductoNombre(agregarDTO.getProductoNombre());
            item.setPrecioCLP(agregarDTO.getPrecioCLP());
            item.setProductoImagen(agregarDTO.getProductoImagen());
            item.setCantidad(agregarDTO.getCantidad());
            item.setStockDisponible(agregarDTO.getStockDisponible());
        }

        CarritoItem savedItem = carritoRepository.save(item);
        return convertToDTO(savedItem);
    }

    /**
     * Actualiza la cantidad de un item en el carrito
     */
    public CarritoItemDTO actualizarCantidad(Long itemId, Integer cantidad) {
        CarritoItem item = carritoRepository.findById(itemId)
                .orElseThrow(() -> new CarritoItemNotFoundException("Item no encontrado con ID: " + itemId));

        if (cantidad <= 0) {
            carritoRepository.delete(item);
            return null;
        }

        item.setCantidad(cantidad);
        CarritoItem updatedItem = carritoRepository.save(item);
        return convertToDTO(updatedItem);
    }

    /**
     * Elimina un item específico del carrito
     */
    public void eliminarItem(Long itemId) {
        if (!carritoRepository.existsById(itemId)) {
            throw new CarritoItemNotFoundException("Item no encontrado con ID: " + itemId);
        }
        carritoRepository.deleteById(itemId);
    }

    /**
     * Elimina un producto del carrito por usuario y código de producto
     */
    public void eliminarItemPorProducto(Long usuarioId, String productoCode) {
        carritoRepository.deleteByUsuarioIdAndProductoCode(usuarioId, productoCode);
    }

    /**
     * Limpia todo el carrito de un usuario
     */
    public void limpiarCarrito(Long usuarioId) {
        carritoRepository.deleteByUsuarioId(usuarioId);
    }

    /**
     * Obtiene el total del carrito de un usuario
     */
    @Transactional(readOnly = true)
    public Integer obtenerTotal(Long usuarioId) {
        List<CarritoItem> items = carritoRepository.findByUsuarioId(usuarioId);
        return items.stream()
                .mapToInt(CarritoItem::getSubtotal)
                .sum();
    }

    /**
     * Obtiene la cantidad total de items en el carrito
     */
    @Transactional(readOnly = true)
    public Integer obtenerCantidadItems(Long usuarioId) {
        List<CarritoItem> items = carritoRepository.findByUsuarioId(usuarioId);
        return items.stream()
                .mapToInt(CarritoItem::getCantidad)
                .sum();
    }

    /**
     * Convierte CarritoItem a CarritoItemDTO
     */
    private CarritoItemDTO convertToDTO(CarritoItem item) {
        return new CarritoItemDTO(
                item.getId(),
                item.getUsuarioId(),
                item.getProductoCode(),
                item.getProductoNombre(),
                item.getPrecioCLP(),
                item.getProductoImagen(),
                item.getCantidad(),
                item.getStockDisponible(),
                item.getFechaAgregado(),
                item.getSubtotal()
        );
    }
}
