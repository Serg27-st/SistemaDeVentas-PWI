package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.service.ExcelExportService;
import com.tulicoreria.licoreria.service.ReporteService;
import com.tulicoreria.licoreria.service.impl.ReporteServiceImpl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

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

    // ── Reporte 1: Ventas por Mes ────────────────────────────────────────────
    @GetMapping("/ventas-mes")
    public String ventasPorMes(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio,
            Model model) {

        // Si no se pasan parámetros, usa el mes y año actual
        if (mes == 0)  mes  = LocalDate.now().getMonthValue();
        if (anio == 0) anio = LocalDate.now().getYear();

        int anioActual = LocalDate.now().getYear();
        model.addAttribute("reporte", reporteService.reporteVentasPorMes(mes, anio));
        model.addAttribute("mes", mes);
        model.addAttribute("anio", anio);
        model.addAttribute("anioActual", anioActual);
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
        model.addAttribute("anioActual", LocalDate.now().getYear());
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
        model.addAttribute("anioActual", LocalDate.now().getYear());
        return "reportes/productos";
    }

    // ── Descargas Excel ──────────────────────────────────────────────────────

    @GetMapping("/ventas-mes/excel")
    public ResponseEntity<byte[]> descargarVentasMesExcel(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) throws IOException {

        if (mes == 0)  mes  = LocalDate.now().getMonthValue();
        if (anio == 0) anio = LocalDate.now().getYear();

        ReporteVentasMesDTO reporte = reporteService.reporteVentasPorMes(mes, anio);
        byte[] bytes = excelExportService.exportarVentasPorMes(reporte);
        String filename = "ventas-mes-" + reporte.getNombreMes() + "-" + anio + ".xlsx";
        return excelResponse(bytes, filename);
    }

    @GetMapping("/clientes/excel")
    public ResponseEntity<byte[]> descargarClientesExcel(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) throws IOException {

        if (mes == 0)  mes  = LocalDate.now().getMonthValue();
        if (anio == 0) anio = LocalDate.now().getYear();

        ReporteClientesDTO reporte = reporteService.reporteVentasPorCliente(mes, anio);
        byte[] bytes = excelExportService.exportarVentasPorCliente(reporte);
        String filename = "clientes-" + reporte.getNombreMes() + "-" + anio + ".xlsx";
        return excelResponse(bytes, filename);
    }

    @GetMapping("/productos/excel")
    public ResponseEntity<byte[]> descargarProductosExcel(
            @RequestParam(defaultValue = "0") int mes,
            @RequestParam(defaultValue = "0") int anio) throws IOException {

        if (mes == 0)  mes  = LocalDate.now().getMonthValue();
        if (anio == 0) anio = LocalDate.now().getYear();

        ReporteProductosDTO reporte = reporteService.reporteVentasPorProducto(mes, anio);
        byte[] bytes = excelExportService.exportarVentasPorProducto(reporte);
        String filename = "productos-" + reporte.getNombreMes() + "-" + anio + ".xlsx";
        return excelResponse(bytes, filename);
    }

    private ResponseEntity<byte[]> excelResponse(byte[] bytes, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
