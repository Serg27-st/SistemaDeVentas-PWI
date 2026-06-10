package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.BusquedaResultDTO;

import java.util.List;

public interface BusquedaService {

    /** Busca productos con tolerancia a errores tipográficos. */
    List<BusquedaResultDTO> buscar(String query, int limit);

    /** Devuelve los términos más buscados (tendencias). */
    List<String> tendencias(int limit);

    /** Registra un término de búsqueda de forma asíncrona (para tendencias). */
    void registrarBusqueda(String termino);
}
