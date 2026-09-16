package com.milsabores.bff.service.impl;

import com.milsabores.bff.client.CarritoClient;
import com.milsabores.bff.client.ProductoClient;
import com.milsabores.bff.client.UsuarioClient;
import com.milsabores.bff.client.VentaClient;
import com.milsabores.bff.dto.*;
import com.milsabores.bff.exception.CarritoVacioException;
import com.milsabores.bff.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta la conversion de un carrito en una venta con pago Transbank
 * iniciado, reemplazando varias llamadas que hoy hace el frontend
 * directamente a carrito-service y ventas-service (ver ANALISIS-ARQUITECTURA.md,
 * "Flujo de Datos Tipico"). El subtotal/IVA/total ya no los arma ni los
 * calcula el frontend: el BFF los deriva del carrito real en carrito-service,
 * usando la misma regla de negocio (IVA 19%, redondeado) que ventas-service
 * espera validar.
 */
@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private static final double IVA_RATE = 0.19;

    private final UsuarioClient usuarioClient;
    private final ProductoClient productoClient;
    private final CarritoClient carritoClient;
    private final VentaClient ventaClient;

    @Override
    public CheckoutResumenDTO obtenerResumen(Long usuarioId) {
        CarritoDTO carrito = obtenerCarritoNoVacio(usuarioId);

        List<CheckoutResumenItemDTO> items = new ArrayList<>();
        int subtotal = 0;
        boolean listoParaPagar = true;

        for (CarritoItemDTO item : carrito.getItems()) {
            ProductoDTO producto = productoClient.obtenerPorCode(item.getProductoCode());
            boolean precioDesactualizado = !producto.getPrecioCLP().equals(item.getPrecioCLP());
            boolean stockInsuficiente = producto.getStock() < item.getCantidad();
            int itemSubtotal = producto.getPrecioCLP() * item.getCantidad();
            subtotal += itemSubtotal;
            if (precioDesactualizado || stockInsuficiente) {
                listoParaPagar = false;
            }

            items.add(new CheckoutResumenItemDTO(
                    item.getProductoCode(),
                    item.getProductoNombre(),
                    item.getProductoImagen(),
                    item.getCantidad(),
                    producto.getPrecioCLP(),
                    itemSubtotal,
                    precioDesactualizado,
                    stockInsuficiente));
        }

        int iva = calcularIva(subtotal);
        int total = subtotal + iva;
        return new CheckoutResumenDTO(usuarioId, items, subtotal, iva, total, listoParaPagar);
    }

    @Override
    public CheckoutResultDTO procesarCheckout(Long usuarioId) {
        UsuarioDTO usuario = usuarioClient.obtenerPorId(usuarioId);
        CarritoDTO carrito = obtenerCarritoNoVacio(usuarioId);

        List<DetalleVentaDTO> detalles = carrito.getItems().stream()
                .map(item -> new DetalleVentaDTO(
                        item.getProductoCode(),
                        item.getProductoNombre(),
                        item.getProductoImagen(),
                        item.getCantidad(),
                        item.getPrecioCLP(),
                        item.getSubtotal()))
                .toList();

        int subtotal = carrito.getTotalPrecio();
        int iva = calcularIva(subtotal);
        int total = subtotal + iva;

        VentaRequestDTO ventaRequest = new VentaRequestDTO(
                usuarioId, usuario.getNombre(), usuario.getEmail(), detalles, subtotal, iva, total);

        VentaResponseDTO venta = ventaClient.crearVenta(ventaRequest);
        TransbankResponseDTO transbank = ventaClient.iniciarPago(venta.getId());

        return new CheckoutResultDTO(
                venta.getId(),
                venta.getEstado(),
                subtotal,
                iva,
                total,
                transbank.getToken(),
                transbank.getUrl(),
                transbank.getOrdenCompra());
    }

    private CarritoDTO obtenerCarritoNoVacio(Long usuarioId) {
        CarritoDTO carrito = carritoClient.obtenerCarrito(usuarioId);
        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            throw new CarritoVacioException("El carrito del usuario " + usuarioId + " esta vacio");
        }
        return carrito;
    }

    private int calcularIva(int subtotal) {
        return (int) Math.round(subtotal * IVA_RATE);
    }
}
