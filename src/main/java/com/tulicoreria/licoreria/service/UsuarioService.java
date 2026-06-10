package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.UsuarioRequestDTO;
import com.tulicoreria.licoreria.dto.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponseDTO> listarTodos();
    UsuarioResponseDTO buscarPorId(Long id);
    UsuarioResponseDTO crear(UsuarioRequestDTO dto);
    void desactivar(Long id);
}
