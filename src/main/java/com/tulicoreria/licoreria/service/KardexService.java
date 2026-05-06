package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.KardexResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface KardexService {

    List<KardexResponseDTO> historialPorProducto(Long productoId);
    List<KardexResponseDTO> ultimosMovimientos(Long productoId);
    List<KardexResponseDTO> movimientosPorRango(LocalDateTime inicio, LocalDateTime fin);
}
