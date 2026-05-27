package com.tulicoreria.licoreria.service;

import java.util.List;

import com.tulicoreria.licoreria.dto.ItemCarritoDTO;
import com.tulicoreria.licoreria.dto.VentaRequestDTO;
import com.tulicoreria.licoreria.dto.VentaResponseDTO;

public interface VentaService {

    VentaResponseDTO registrar(VentaRequestDTO dto);

    /** Registra una venta originada desde el carrito de la tienda pública. */
    VentaResponseDTO registrarDesdeCarrito(List<ItemCarritoDTO> items, String metodoPago);

    /** Marca un pedido PENDIENTE como COMPLETADA (entregado al cliente). */
    VentaResponseDTO completar(Long id);

    VentaResponseDTO anular(Long id);
    List<VentaResponseDTO> listarTodas();
    VentaResponseDTO buscarPorId(Long id);
    VentaResponseDTO buscarPorComprobante(String numeroComprobante);
    VentaResponseDTO toDTO(com.tulicoreria.licoreria.model.Venta venta);
}
