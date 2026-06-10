package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.CategoriaRequestDTO;
import com.tulicoreria.licoreria.dto.CategoriaResponseDTO;

import java.util.List;

public interface CategoriaService {

    List<CategoriaResponseDTO> listarTodas();
    List<CategoriaResponseDTO> listarActivas();
    CategoriaResponseDTO buscarPorId(Long id);
    CategoriaResponseDTO crear(CategoriaRequestDTO dto);
    CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto);
    void desactivar(Long id);
}
