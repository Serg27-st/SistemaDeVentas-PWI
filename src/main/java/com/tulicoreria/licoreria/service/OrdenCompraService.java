package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.OrdenCompraRequestDTO;
import com.tulicoreria.licoreria.dto.OrdenCompraResponseDTO;

import java.util.List;

public interface OrdenCompraService {

    OrdenCompraResponseDTO crear(OrdenCompraRequestDTO dto);
    OrdenCompraResponseDTO recibirMercaderia(Long id, String comprobanteProveedor);
    OrdenCompraResponseDTO anular(Long id);
    List<OrdenCompraResponseDTO> listarTodas();
    List<OrdenCompraResponseDTO> listarPendientes();
    OrdenCompraResponseDTO buscarPorId(Long id);
}
