package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.ReclamacionRequestDTO;
import com.tulicoreria.licoreria.dto.ReclamacionResponseDTO;

import java.util.List;

public interface ReclamacionService {

    ReclamacionResponseDTO registrar(ReclamacionRequestDTO dto);

    List<ReclamacionResponseDTO> listarTodas();

    ReclamacionResponseDTO atender(Long id);

    long contarPendientes();
}
