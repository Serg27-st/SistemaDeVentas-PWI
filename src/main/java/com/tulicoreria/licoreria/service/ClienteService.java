package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.ClienteRequestDTO;
import com.tulicoreria.licoreria.dto.ClienteResponseDTO;
import com.tulicoreria.licoreria.model.Cliente;

import java.util.List;

public interface ClienteService {

    List<ClienteResponseDTO> listarTodos();
    ClienteResponseDTO buscarPorId(Long id);
    ClienteResponseDTO buscarPorDocumento(String numeroDocumento);
    List<ClienteResponseDTO> buscarPorNombre(String nombre);
    ClienteResponseDTO crear(ClienteRequestDTO dto);
    ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto);

    // Usado internamente por VentaService
    Cliente obtenerEntidad(Long id);
    ClienteResponseDTO toDTO(Cliente cliente);
}
