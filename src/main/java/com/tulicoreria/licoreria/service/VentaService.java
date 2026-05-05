package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.VentaRequestDTO;
import com.tulicoreria.licoreria.dto.VentaResponseDTO;

import java.util.List;

public interface VentaService {

    VentaResponseDTO registrar(VentaRequestDTO dto);
    VentaResponseDTO anular(Long id);
    List<VentaResponseDTO> listarTodas();
    VentaResponseDTO buscarPorId(Long id);
    VentaResponseDTO buscarPorComprobante(String numeroComprobante);
}