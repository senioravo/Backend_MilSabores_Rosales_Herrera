package com.milsabores.producto.service;

import com.milsabores.producto.dto.ProductoDTO;
import com.milsabores.producto.dto.ProductoResponseDTO;
import com.milsabores.producto.dto.ProductoPaginadoResponseDTO;
import com.milsabores.producto.dto.CategoriaDTO;
import com.milsabores.producto.exception.ProductoNotFoundException;
import com.milsabores.producto.exception.CategoriaNotFoundException;
import com.milsabores.producto.model.Producto;
import com.milsabores.producto.model.Categoria;
import com.milsabores.producto.repository.ProductoRepository;
import com.milsabores.producto.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    /**
     * Obtiene todos los productos activos
     */
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerTodos() {
        return productoRepository.findByActivoTrue().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un producto por su código
     */
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorCodigo(String code) {
        Producto producto = productoRepository.findById(code)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con código: " + code));
        return convertToResponseDTO(producto);
    }

    /**
     * Obtiene productos por categoría
     */
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerPorCategoria(String categoriaId) {
        return productoRepository.findByCategoriaIdAndActivoTrue(categoriaId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene productos destacados (con stock disponible)
     */
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerDestacados(Integer limit) {
        List<Producto> productos = productoRepository.findFeaturedProducts();
        if (limit != null && limit > 0) {
            productos = productos.stream().limit(limit).collect(Collectors.toList());
        }
        return productos.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca productos por nombre
     */
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarPorNombre(String nombre) {
        return productoRepository.buscarPorNombre(nombre).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo producto
     */
    public ProductoResponseDTO crear(ProductoDTO productoDTO) {
        Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
                .orElseThrow(() -> new CategoriaNotFoundException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));

        Producto producto = new Producto();
        producto.setCode(productoDTO.getCode());
        producto.setNombre(productoDTO.getNombre());
        producto.setCategoria(categoria);
        producto.setTipoForma(productoDTO.getTipoForma());
        producto.setTamanosDisponibles(productoDTO.getTamanosDisponibles());
        producto.setPrecioCLP(productoDTO.getPrecioCLP());
        producto.setStock(productoDTO.getStock());
        producto.setPersonalizable(productoDTO.getPersonalizable() != null ? productoDTO.getPersonalizable() : false);
        producto.setMaxMsgChars(productoDTO.getMaxMsgChars() != null ? productoDTO.getMaxMsgChars() : 0);
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setEtiquetas(productoDTO.getEtiquetas());
        producto.setImagen(productoDTO.getImagen());
        producto.setActivo(true);

        Producto savedProducto = productoRepository.save(producto);
        return convertToResponseDTO(savedProducto);
    }

    /**
     * Actualiza un producto existente
     */
    public ProductoResponseDTO actualizar(String code, ProductoDTO productoDTO) {
        Producto producto = productoRepository.findById(code)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con código: " + code));

        if (productoDTO.getCategoriaId() != null && !productoDTO.getCategoriaId().equals(producto.getCategoria().getId())) {
            Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
                    .orElseThrow(() -> new CategoriaNotFoundException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));
            producto.setCategoria(categoria);
        }

        producto.setNombre(productoDTO.getNombre());
        producto.setTipoForma(productoDTO.getTipoForma());
        producto.setTamanosDisponibles(productoDTO.getTamanosDisponibles());
        producto.setPrecioCLP(productoDTO.getPrecioCLP());
        producto.setStock(productoDTO.getStock());
        producto.setPersonalizable(productoDTO.getPersonalizable());
        producto.setMaxMsgChars(productoDTO.getMaxMsgChars());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setEtiquetas(productoDTO.getEtiquetas());
        producto.setImagen(productoDTO.getImagen());

        Producto updatedProducto = productoRepository.save(producto);
        return convertToResponseDTO(updatedProducto);
    }

    /**
     * Obtiene productos con paginación y filtros
     */
    @Transactional(readOnly = true)
    public ProductoPaginadoResponseDTO<ProductoResponseDTO> obtenerProductosPaginados(
            int page, int size, String categoriaId, Integer minPrecio, Integer maxPrecio,
            Boolean personalizable, String sortBy, String sortDir) {
        
        // Configurar ordenamiento
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isEmpty()) {
            sort = Sort.by(sortBy);
            if ("desc".equalsIgnoreCase(sortDir)) {
                sort = sort.descending();
            } else {
                sort = sort.ascending();
            }
        }
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Producto> productoPage;
        
        // Aplicar filtros
        if (categoriaId != null || minPrecio != null || maxPrecio != null || personalizable != null) {
            productoPage = productoRepository.findAll((root, query, criteriaBuilder) -> {
                var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
                
                // Filtro por activo
                predicates.add(criteriaBuilder.isTrue(root.get("activo")));
                
                // Filtro por categoría
                if (categoriaId != null) {
                    predicates.add(criteriaBuilder.equal(root.get("categoria").get("id"), categoriaId));
                }
                
                // Filtro por rango de precio
                if (minPrecio != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("precioCLP"), minPrecio));
                }
                if (maxPrecio != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("precioCLP"), maxPrecio));
                }
                
                // Filtro por personalizable
                if (personalizable != null) {
                    predicates.add(criteriaBuilder.equal(root.get("personalizable"), personalizable));
                }
                
                return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            }, pageable);
        } else {
            productoPage = productoRepository.findByActivoTrue(pageable);
        }
        
        // Convertir a DTO
        List<ProductoResponseDTO> content = productoPage.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return new ProductoPaginadoResponseDTO<>(
                content,
                productoPage.getNumber(),
                productoPage.getTotalPages(),
                productoPage.getTotalElements(),
                productoPage.getSize(),
                productoPage.isFirst(),
                productoPage.isLast(),
                productoPage.isEmpty()
        );
    }
    
    /**
     * Actualiza el stock de un producto
     */
    public ProductoResponseDTO actualizarStock(String code, Integer nuevoStock) {
        Producto producto = productoRepository.findById(code)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con código: " + code));
        
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        
        producto.setStock(nuevoStock);
        Producto updatedProducto = productoRepository.save(producto);
        return convertToResponseDTO(updatedProducto);
    }
    
    /**
     * Reduce el stock de un producto cuando es vendido
     */
    public ProductoResponseDTO reducirStock(String code, Integer cantidad) {
        Producto producto = productoRepository.findById(code)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con código: " + code));
        
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        
        int nuevoStock = producto.getStock() - cantidad;
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("Stock insuficiente. Stock actual: " + producto.getStock() + ", Cantidad solicitada: " + cantidad);
        }
        
        producto.setStock(nuevoStock);
        Producto updatedProducto = productoRepository.save(producto);
        return convertToResponseDTO(updatedProducto);
    }

    /**
     * Elimina (desactiva) un producto
     */
    public void eliminar(String code) {
        Producto producto = productoRepository.findById(code)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con código: " + code));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    /**
     * Convierte Producto a ProductoResponseDTO
     */
    private ProductoResponseDTO convertToResponseDTO(Producto producto) {
        CategoriaDTO categoriaDTO = new CategoriaDTO(
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                producto.getCategoria().getDescripcion(),
                producto.getCategoria().getImagen()
        );

        return new ProductoResponseDTO(
                producto.getCode(),
                producto.getNombre(),
                categoriaDTO,
                producto.getTipoForma(),
                producto.getTamanosDisponibles(),
                producto.getPrecioCLP(),
                producto.getStock(),
                producto.getPersonalizable(),
                producto.getMaxMsgChars(),
                producto.getDescripcion(),
                producto.getEtiquetas(),
                producto.getImagen(),
                producto.getActivo()
        );
    }
}
