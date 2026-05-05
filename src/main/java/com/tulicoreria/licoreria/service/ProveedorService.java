package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.ProveedorRequestDTO;
import com.tulicoreria.licoreria.dto.ProveedorResponseDTO;

import java.util.List;

public interface ProveedorService {

    List<ProveedorResponseDTO> listarTodos();
    List<ProveedorResponseDTO> listarActivos();
    ProveedorResponseDTO buscarPorId(Long id);
    ProveedorResponseDTO buscarPorRuc(String ruc);
    ProveedorResponseDTO crear(ProveedorRequestDTO dto);
    ProveedorResponseDTO actualizar(Long id, ProveedorRequestDTO dto);
    void desactivar(Long id);
}