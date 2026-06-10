package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.service.impl.ReporteServiceImpl.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

@Service
public class ExcelExportService {

    // ════════════════════════════════════════════════════════════════════════
    // REPORTE 1 — VENTAS POR MES
    // ════════════════════════════════════════════════════════════════════════
    public byte[] exportarVentasPorMes(ReporteVentasMesDTO reporte) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet(reporte.getNombreMes() + " " + reporte.getAnio());

            CellStyle headerStyle   = headerStyle(wb);
            CellStyle currencyStyle = currencyStyle(wb);
            CellStyle boldStyle     = boldStyle(wb);
            CellStyle titleStyle    = titleStyle(wb);

            int r = 0;

            // Título
            r = writeTitle(sheet, r, "Reporte de Ventas — " + reporte.getNombreMes() + " " + reporte.getAnio(), titleStyle, 3);

            // Resumen general
            Row h = sheet.createRow(r++);
            headerCell(h, 0, "Ventas realizadas", headerStyle);
            headerCell(h, 1, "Total facturado",   headerStyle);
            headerCell(h, 2, "IGV (18%)",         headerStyle);
            headerCell(h, 3, "Ticket promedio",   headerStyle);

            Row d = sheet.createRow(r++);
            d.createCell(0).setCellValue(reporte.getCantidadVentas());
            currencyCell(d, 1, reporte.getTotal(),         currencyStyle);
            currencyCell(d, 2, reporte.getIgv(),           currencyStyle);
            currencyCell(d, 3, reporte.getTicketPromedio(), currencyStyle);
            r++;

            // Por tipo de comprobante
            r = writeSectionTitle(sheet, r, "Por Tipo de Comprobante", boldStyle);
            Row ch = sheet.createRow(r++);
            headerCell(ch, 0, "Tipo Comprobante", headerStyle);
            headerCell(ch, 1, "Cantidad Ventas",  headerStyle);
            headerCell(ch, 2, "Total",            headerStyle);
            headerCell(ch, 3, "% del Total",      headerStyle);
            for (ResumenComprobanteDTO c : reporte.getPorTipoComprobante()) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(c.getTipoComprobante());
                row.createCell(1).setCellValue(c.getCantidadVentas());
                currencyCell(row, 2, c.getTotal(), currencyStyle);
                row.createCell(3).setCellValue(c.getPorcentaje() + "%");
            }
            r++;

            // Por método de pago
            r = writeSectionTitle(sheet, r, "Por Método de Pago", boldStyle);
            Row ph = sheet.createRow(r++);
            headerCell(ph, 0, "Método de Pago",  headerStyle);
            headerCell(ph, 1, "Cantidad Ventas", headerStyle);
            headerCell(ph, 2, "Total",           headerStyle);
            headerCell(ph, 3, "% del Total",     headerStyle);
            for (ResumenMetodoPagoDTO p : reporte.getPorMetodoPago()) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(p.getMetodoPago());
                row.createCell(1).setCellValue(p.getCantidadVentas());
                currencyCell(row, 2, p.getTotal(), currencyStyle);
                row.createCell(3).setCellValue(p.getPorcentaje() + "%");
            }
            r++;

            // Desglose diario
            r = writeSectionTitle(sheet, r, "Desglose Diario", boldStyle);
            Row dh = sheet.createRow(r++);
            headerCell(dh, 0, "Día",              headerStyle);
            headerCell(dh, 1, "Fecha",            headerStyle);
            headerCell(dh, 2, "Cantidad Ventas",  headerStyle);
            headerCell(dh, 3, "Total del Día",    headerStyle);
            for (ResumenDiaDTO dia : reporte.getPorDia()) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(dia.getDia());
                row.createCell(1).setCellValue(dia.getDia() + " de " + reporte.getNombreMes() + " " + reporte.getAnio());
                row.createCell(2).setCellValue(dia.getCantidadVentas());
                currencyCell(row, 3, dia.getTotal(), currencyStyle);
            }

            autoSize(sheet, 4);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // REPORTE 2 — VENTAS POR CLIENTE
    // ════════════════════════════════════════════════════════════════════════
    public byte[] exportarVentasPorCliente(ReporteClientesDTO reporte) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet(reporte.getNombreMes() + " " + reporte.getAnio());

            CellStyle headerStyle   = headerStyle(wb);
            CellStyle currencyStyle = currencyStyle(wb);
            CellStyle titleStyle    = titleStyle(wb);

            int r = 0;

            r = writeTitle(sheet, r, "Reporte por Cliente — " + reporte.getNombreMes() + " " + reporte.getAnio(), titleStyle, 6);

            // Resumen
            Row sh = sheet.createRow(r++);
            headerCell(sh, 0, "Clientes del mes", headerStyle);
            headerCell(sh, 1, "Total vendido",    headerStyle);
            headerCell(sh, 2, "Gasto promedio",   headerStyle);

            Row sd = sheet.createRow(r++);
            sd.createCell(0).setCellValue(reporte.getTotalClientes());
            currencyCell(sd, 1, reporte.getTotalVendido(),  currencyStyle);
            currencyCell(sd, 2, reporte.getTicketPromedio(), currencyStyle);
            r++;

            // Ranking
            Row rh = sheet.createRow(r++);
            headerCell(rh, 0, "#",                  headerStyle);
            headerCell(rh, 1, "Cliente",             headerStyle);
            headerCell(rh, 2, "Tipo Documento",      headerStyle);
            headerCell(rh, 3, "Número Documento",    headerStyle);
            headerCell(rh, 4, "Compras",             headerStyle);
            headerCell(rh, 5, "Total Gastado",       headerStyle);
            headerCell(rh, 6, "% del Total",         headerStyle);

            int pos = 1;
            for (ReporteClienteItemDTO c : reporte.getClientes()) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(pos++);
                row.createCell(1).setCellValue(c.getNombreCompleto());
                row.createCell(2).setCellValue(c.getTipoDocumento());
                row.createCell(3).setCellValue(c.getNumeroDocumento());
                row.createCell(4).setCellValue(c.getCantidadCompras());
                currencyCell(row, 5, c.getTotalGastado(), currencyStyle);
                row.createCell(6).setCellValue(c.getPorcentaje() + "%");
            }

            autoSize(sheet, 7);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // REPORTE 3 — VENTAS POR PRODUCTO
    // ════════════════════════════════════════════════════════════════════════
    public byte[] exportarVentasPorProducto(ReporteProductosDTO reporte) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet(reporte.getNombreMes() + " " + reporte.getAnio());

            CellStyle headerStyle   = headerStyle(wb);
            CellStyle currencyStyle = currencyStyle(wb);
            CellStyle titleStyle    = titleStyle(wb);

            int r = 0;

            r = writeTitle(sheet, r, "Reporte por Producto — " + reporte.getNombreMes() + " " + reporte.getAnio(), titleStyle, 8);

            // Resumen
            Row sh = sheet.createRow(r++);
            headerCell(sh, 0, "Productos vendidos",    headerStyle);
            headerCell(sh, 1, "Unidades despachadas",  headerStyle);
            headerCell(sh, 2, "Ingresos totales",      headerStyle);

            Row sd = sheet.createRow(r++);
            sd.createCell(0).setCellValue(reporte.getTotalProductosVendidos());
            sd.createCell(1).setCellValue(reporte.getTotalUnidades());
            currencyCell(sd, 2, reporte.getTotalIngresos(), currencyStyle);
            r++;

            // Ranking
            Row rh = sheet.createRow(r++);
            headerCell(rh, 0, "#",                   headerStyle);
            headerCell(rh, 1, "Producto",             headerStyle);
            headerCell(rh, 2, "Marca",                headerStyle);
            headerCell(rh, 3, "Categoría",            headerStyle);
            headerCell(rh, 4, "Cantidad",             headerStyle);
            headerCell(rh, 5, "Unidad",               headerStyle);
            headerCell(rh, 6, "Unidades vendidas",    headerStyle);
            headerCell(rh, 7, "Ingresos",             headerStyle);
            headerCell(rh, 8, "Stock actual",         headerStyle);
            headerCell(rh, 9, "% Ingresos",           headerStyle);

            int pos = 1;
            for (ReporteProductoItemDTO p : reporte.getProductos()) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(pos++);
                row.createCell(1).setCellValue(p.getNombre());
                row.createCell(2).setCellValue(p.getMarca() != null ? p.getMarca() : "");
                row.createCell(3).setCellValue(p.getCategoria());
                // Cantidad: mostrar sin decimales si es entero
                if (p.getCantidad() != null) {
                    double c = p.getCantidad();
                    row.createCell(4).setCellValue(c % 1 == 0 ? (long) c : c);
                } else {
                    row.createCell(4).setCellValue("");
                }
                row.createCell(5).setCellValue(p.getUnidadMedida() != null ? p.getUnidadMedida() : "");
                row.createCell(6).setCellValue(p.getUnidadesVendidas());
                currencyCell(row, 7, p.getIngresos(), currencyStyle);
                row.createCell(8).setCellValue(p.getStockActual() != null ? p.getStockActual() : 0);
                row.createCell(9).setCellValue(p.getPorcentaje() + "%");
            }

            autoSize(sheet, 10);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private int writeTitle(Sheet sheet, int rowNum, String text, CellStyle style, int lastCol) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, lastCol));
        return rowNum + 2;
    }

    private int writeSectionTitle(Sheet sheet, int rowNum, String text, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        cell.setCellStyle(style);
        return rowNum + 1;
    }

    private void headerCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void currencyCell(Row row, int col, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value.doubleValue() : 0.0);
        cell.setCellStyle(style);
    }

    private void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) sheet.autoSizeColumn(i);
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle currencyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("\"S/. \"#,##0.00"));
        return style;
    }

    private CellStyle boldStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        return style;
    }

    private CellStyle titleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }
}
