package com.tulicoreria.licoreria.service;

import java.util.List;

import com.tulicoreria.licoreria.dto.CategoriaResponseDTO;

/**
 * Holder temporal para evitar acoplar la carga de datos pública.
 * 
 * Nota: se reemplazará por inyección directa si prefieres.
 */
public final class CategoriaServiceHolder {

    private CategoriaServiceHolder() {
    }

    // Placeholder: se sobreescribe desde el controlador mediante inyección manual
    public static List<CategoriaResponseDTO> categoriasActivas() {
        return List.of();
    }
}

