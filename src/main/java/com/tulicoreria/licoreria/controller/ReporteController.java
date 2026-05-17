package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final ReporteService reporteService;

    // Menú de reportes
    @GetMapping
    public String menu() {
        return "reportes/menu";
    }

    // ── Reporte 1: Ventas por Mes ────────────────────────────────────────────
    @GetMapping("/ventas-mes")
    public String ventasPorMes(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio,
            Model model) {

        // Si no se pasan parámetros, usa el mes y año actual
        if (mes == 0)  mes  = LocalDate.now().getMonthValue();
        if (anio == 0) anio = LocalDate.now().getYear();

        model.addAttribute("reporte", reporteService.reporteVentasPorMes(mes, anio));
        model.addAttribute("mes", mes);
        model.addAttribute("anio", anio);
        return "reportes/ventas-mes";
    }

    // ── Reporte 2: Ventas por Cliente ────────────────────────────────────────
    @GetMapping("/clientes")
    public String ventasPorCliente(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio,
            Model model) {

        if (mes == 0)  mes  = LocalDate.now().getMonthValue();
        if (anio == 0) anio = LocalDate.now().getYear();

        model.addAttribute("reporte", reporteService.reporteVentasPorCliente(mes, anio));
        model.addAttribute("mes", mes);
        model.addAttribute("anio", anio);
        return "reportes/clientes";
    }

    // ── Reporte 3: Ventas por Producto ───────────────────────────────────────
    @GetMapping("/productos")
    public String ventasPorProducto(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio,
            Model model) {

        if (mes == 0)  mes  = LocalDate.now().getMonthValue();
        if (anio == 0) anio = LocalDate.now().getYear();

        model.addAttribute("reporte", reporteService.reporteVentasPorProducto(mes, anio));
        model.addAttribute("mes", mes);
        model.addAttribute("anio", anio);
        return "reportes/productos";
    }
}
