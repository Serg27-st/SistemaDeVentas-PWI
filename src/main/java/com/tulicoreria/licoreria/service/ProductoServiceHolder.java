package com.tulicoreria.licoreria.service;

import java.util.List;

import com.tulicoreria.licoreria.dto.ProductoResponseDTO;

public final class ProductoServiceHolder {

    private ProductoServiceHolder() {
    }

    public static List<ProductoResponseDTO> destacados() {
        // placeholder: usa listarTodos o listarConStockBajo si no tienes un método 'destacados'
        return List.of();
    }

    public static List<ProductoResponseDTO> catalogo(Long categoriaId, String q) {
        // placeholder: devuelve vacío para evitar errores si todavía no mapeamos filtro.
        return List.of();
    }
}

