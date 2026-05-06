package com.tulicoreria.licoreria.service;

import java.util.List;

import com.tulicoreria.licoreria.dto.VentaRequestDTO;
import com.tulicoreria.licoreria.dto.VentaResponseDTO;

public interface VentaService {

    VentaResponseDTO registrar(VentaRequestDTO dto);
    VentaResponseDTO anular(Long id);
    List<VentaResponseDTO> listarTodas();
    VentaResponseDTO buscarPorId(Long id);
    VentaResponseDTO buscarPorComprobante(String numeroComprobante);
    VentaResponseDTO toDTO(com.tulicoreria.licoreria.model.Venta venta);
}
