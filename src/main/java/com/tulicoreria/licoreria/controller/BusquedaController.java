package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.BusquedaResultDTO;
import com.tulicoreria.licoreria.service.BusquedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST de búsqueda — usada por el autocompletado del frontend.
 * Ruta pública: no requiere autenticación.
 */
@RestController
@RequestMapping("/api/buscar")
@RequiredArgsConstructor
public class BusquedaController {

    private final BusquedaService busquedaService;

    /**
     * Búsqueda fuzzy de productos.
     * Ejemplo: GET /api/buscar?q=yoniwalker&limit=8
     */
    @GetMapping
    public List<BusquedaResultDTO> buscar(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "8") int limit) {
        if (!q.isBlank()) {
            busquedaService.registrarBusqueda(q);
        }
        return busquedaService.buscar(q, Math.min(limit, 20));
    }

    /**
     * Top tendencias para mostrar cuando el buscador está vacío.
     * Ejemplo: GET /api/buscar/tendencias
     */
    @GetMapping("/tendencias")
    public List<String> tendencias() {
        return busquedaService.tendencias(6);
    }
}
