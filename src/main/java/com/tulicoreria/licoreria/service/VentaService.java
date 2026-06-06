package com.tulicoreria.licoreria.service;

import java.util.List;

import com.tulicoreria.licoreria.dto.ItemCarritoDTO;
import com.tulicoreria.licoreria.dto.VentaRequestDTO;
import com.tulicoreria.licoreria.dto.VentaResponseDTO;
import com.tulicoreria.licoreria.model.Pedido;

public interface VentaService {

    VentaResponseDTO registrar(VentaRequestDTO dto);

    /** Registra una venta originada desde el carrito (anónimo). */
    VentaResponseDTO registrarDesdeCarrito(List<ItemCarritoDTO> items, String metodoPago);

    /** Registra una venta del carrito vinculada a un cliente registrado. */
    VentaResponseDTO registrarDesdeCarrito(List<ItemCarritoDTO> items, String metodoPago, Long clienteId);

    /** Registra una venta del carrito con envío y, opcionalmente, cargo Culqi ya procesado. */
    VentaResponseDTO registrarDesdeCarrito(List<ItemCarritoDTO> items, String metodoPago, Long clienteId,
                                           java.math.BigDecimal costoEnvio, String distritoEnvio,
                                           String culqiChargeId);

    /** Crea una Venta en el sistema admin a partir de un Pedido web ya confirmado. */
    VentaResponseDTO crearDesdePedido(Pedido pedido);

    /** Marca un pedido PENDIENTE como COMPLETADA (entregado al cliente). */
    VentaResponseDTO completar(Long id);

    VentaResponseDTO anular(Long id);
    List<VentaResponseDTO> listarTodas();
    VentaResponseDTO buscarPorId(Long id);
    VentaResponseDTO buscarPorComprobante(String numeroComprobante);
    VentaResponseDTO toDTO(com.tulicoreria.licoreria.model.Venta venta);
}
