package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.service.ExcelExportService;
import com.tulicoreria.licoreria.service.ReporteService;
import com.tulicoreria.licoreria.service.impl.ReporteServiceImpl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final ReporteService reporteService;
    private final ExcelExportService excelExportService;

    // Menú de reportes
    @GetMapping
    public String menu() {
        return "reportes/menu";
    }

    // ── Reporte 1: Ventas por Período ────────────────────────────────────────
    @GetMapping("/ventas-mes")
    public String ventasPorMes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        LocalDate[] rango = normalizarRango(fechaInicio, fechaFin);
        fechaInicio = rango[0];
        fechaFin = rango[1];

        model.addAttribute("reporte", reporteService.reporteVentasPorMes(fechaInicio, fechaFin));
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        return "reportes/ventas-mes";
    }

    // ── Reporte 2: Ventas por Cliente ────────────────────────────────────────
    @GetMapping("/clientes")
    public String ventasPorCliente(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        LocalDate[] rango = normalizarRango(fechaInicio, fechaFin);
        fechaInicio = rango[0];
        fechaFin = rango[1];

        model.addAttribute("reporte", reporteService.reporteVentasPorCliente(fechaInicio, fechaFin));
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        return "reportes/clientes";
    }

    // ── Reporte 3: Ventas por Producto ───────────────────────────────────────
    @GetMapping("/productos")
    public String ventasPorProducto(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        LocalDate[] rango = normalizarRango(fechaInicio, fechaFin);
        fechaInicio = rango[0];
        fechaFin = rango[1];

        model.addAttribute("reporte", reporteService.reporteVentasPorProducto(fechaInicio, fechaFin));
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        return "reportes/productos";
    }

    // ── Descargas Excel ──────────────────────────────────────────────────────

    @GetMapping("/ventas-mes/excel")
    public ResponseEntity<byte[]> descargarVentasMesExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) throws IOException {

        LocalDate[] rango = normalizarRango(fechaInicio, fechaFin);
        fechaInicio = rango[0];
        fechaFin = rango[1];

        ReporteVentasMesDTO reporte = reporteService.reporteVentasPorMes(fechaInicio, fechaFin);
        byte[] bytes = excelExportService.exportarVentasPorMes(reporte);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String filename = "ventas-" + fechaInicio.format(fmt) + "_a_" + fechaFin.format(fmt) + ".xlsx";
        return excelResponse(bytes, filename);
    }

    @GetMapping("/clientes/excel")
    public ResponseEntity<byte[]> descargarClientesExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) throws IOException {

        LocalDate[] rango = normalizarRango(fechaInicio, fechaFin);
        fechaInicio = rango[0];
        fechaFin = rango[1];

        ReporteClientesDTO reporte = reporteService.reporteVentasPorCliente(fechaInicio, fechaFin);
        byte[] bytes = excelExportService.exportarVentasPorCliente(reporte);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String filename = "clientes-" + fechaInicio.format(fmt) + "_a_" + fechaFin.format(fmt) + ".xlsx";
        return excelResponse(bytes, filename);
    }

    @GetMapping("/productos/excel")
    public ResponseEntity<byte[]> descargarProductosExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) throws IOException {

        LocalDate[] rango = normalizarRango(fechaInicio, fechaFin);
        fechaInicio = rango[0];
        fechaFin = rango[1];

        ReporteProductosDTO reporte = reporteService.reporteVentasPorProducto(fechaInicio, fechaFin);
        byte[] bytes = excelExportService.exportarVentasPorProducto(reporte);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String filename = "productos-" + fechaInicio.format(fmt) + "_a_" + fechaFin.format(fmt) + ".xlsx";
        return excelResponse(bytes, filename);
    }

    // Si no se pasan fechas, usa el mes actual (día 1 → hoy); si el inicio es
    // posterior al fin, se intercambian.
    private LocalDate[] normalizarRango(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDate hoy = LocalDate.now();
        if (fechaInicio == null) fechaInicio = hoy.withDayOfMonth(1);
        if (fechaFin == null) fechaFin = hoy;
        if (fechaFin.isBefore(fechaInicio)) {
            LocalDate tmp = fechaInicio;
            fechaInicio = fechaFin;
            fechaFin = tmp;
        }
        return new LocalDate[]{fechaInicio, fechaFin};
    }

    private ResponseEntity<byte[]> excelResponse(byte[] bytes, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
