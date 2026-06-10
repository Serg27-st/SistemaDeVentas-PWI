package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.service.KardexService;
import com.tulicoreria.licoreria.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/kardex")
@RequiredArgsConstructor
public class KardexController {

    private final KardexService kardexService;
    private final ProductoService productoService;

    // Historial completo de un producto
    @GetMapping("/producto/{id}")
    public String historialProducto(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.buscarPorId(id));
        model.addAttribute("movimientos", kardexService.historialPorProducto(id));
        return "kardex/historial";
    }

    // Movimientos en un rango de fechas
    @GetMapping
    public String movimientosPorRango(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            Model model) {

        // Si no se pasan fechas, muestra los del día actual
        if (inicio == null) inicio = LocalDateTime.now().withHour(0).withMinute(0);
        if (fin == null)    fin    = LocalDateTime.now().withHour(23).withMinute(59);

        model.addAttribute("movimientos", kardexService.movimientosPorRango(inicio, fin));
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);
        model.addAttribute("productos", productoService.listarTodos());
        return "kardex/lista";
    }
}
