package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.dto.DashboardDTO;
import com.tulicoreria.licoreria.model.Cliente;
import com.tulicoreria.licoreria.model.OrdenCompra.EstadoOrden;
import com.tulicoreria.licoreria.model.Producto;
import com.tulicoreria.licoreria.model.Venta;
import com.tulicoreria.licoreria.repository.*;
import com.tulicoreria.licoreria.service.ReporteService;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final OrdenCompraRepository ordenCompraRepository;

    // ════════════════════════════════════════════════════════════════════════
    // REPORTE 1 — VENTAS POR PERÍODO
    // ════════════════════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public ReporteVentasMesDTO reporteVentasPorMes(LocalDate fechaInicio, LocalDate fechaFin) {

        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin    = fechaFin.atTime(23, 59, 59);

        // Totales del período — la query devuelve List<Object[]> con 1 fila siempre
        List<Object[]> totalesList = ventaRepository.findTotalesPorRango(inicio, fin);
        Object[] totales  = totalesList.isEmpty() ? new Object[]{0, 0, 0} : totalesList.get(0);
        BigDecimal subtotal = toBigDecimal(totales, 0);
        BigDecimal igv      = toBigDecimal(totales, 1);
        BigDecimal total    = toBigDecimal(totales, 2);

        Long cantidadVentas  = ventaRepository.countVentasByFechaHoraBetween(inicio, fin);
        if (cantidadVentas == null) cantidadVentas = 0L;

        BigDecimal ticketPromedio = cantidadVentas > 0
                ? total.divide(BigDecimal.valueOf(cantidadVentas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Por tipo de comprobante — se calcula el % sobre el total del período
        List<ResumenComprobanteDTO> comprobantes = ventaRepository
                .findResumenPorTipoComprobanteEnRango(inicio, fin).stream()
                .map(row -> {
                    BigDecimal rowTotal = toBigDecimal(row, 2);
                    int pct = calcularPorcentaje(rowTotal, total);
                    return ResumenComprobanteDTO.builder()
                            .tipoComprobante(row[0].toString())
                            .cantidadVentas(toLong(row[1]))
                            .total(rowTotal)
                            .porcentaje(pct)
                            .build();
                })
                .toList();

        // Por método de pago
        List<ResumenMetodoPagoDTO> metodosPago = ventaRepository
                .findResumenPorMetodoPagoEnRango(inicio, fin).stream()
                .map(row -> {
                    BigDecimal rowTotal = toBigDecimal(row, 2);
                    int pct = calcularPorcentaje(rowTotal, total);
                    return ResumenMetodoPagoDTO.builder()
                            .metodoPago(row[0].toString())
                            .cantidadVentas(toLong(row[1]))
                            .total(rowTotal)
                            .porcentaje(pct)
                            .build();
                })
                .toList();

        // Desglose diario
        List<ResumenDiaDTO> porDia = ventaRepository
                .findResumenPorDiaEnRango(inicio, fin).stream()
                .map(row -> ResumenDiaDTO.builder()
                        .fecha(toLocalDate(row[0]))
                        .cantidadVentas(toLong(row[1]))
                        .total(toBigDecimal(row, 2))
                        .build())
                .toList();

        return ReporteVentasMesDTO.builder()
                .fechaInicio(fechaInicio).fechaFin(fechaFin)
                .cantidadVentas(cantidadVentas)
                .subtotal(subtotal).igv(igv).total(total)
                .ticketPromedio(ticketPromedio)
                .porTipoComprobante(comprobantes)
                .porMetodoPago(metodosPago)
                .porDia(porDia)
                .detalleVentas(buildDetalleVentas(inicio, fin))
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // REPORTE 2 — VENTAS POR CLIENTE
    // ════════════════════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public ReporteClientesDTO reporteVentasPorCliente(LocalDate fechaInicio, LocalDate fechaFin) {

        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin    = fechaFin.atTime(23, 59, 59);

        // Primero construimos los items sin porcentaje para calcular el total
        List<Object[]> rows = clienteRepository.findClientesMasCompradoresPorRango(inicio, fin);

        List<ReporteClienteItemDTO> itemsSinPct = rows.stream()
                .map(row -> {
                    Cliente c = (Cliente) row[0];
                    return ReporteClienteItemDTO.builder()
                            .clienteId(c.getId())
                            .nombreCompleto(c.getNombre() + " " + c.getApellido())
                            .tipoDocumento(c.getTipoDocumento().name())
                            .numeroDocumento(c.getNumeroDocumento())
                            .cantidadCompras(toLong(row[1]))
                            .totalGastado(toBigDecimal(row, 2))
                            .porcentaje(0)
                            .build();
                }).toList();

        BigDecimal totalPeriodo = itemsSinPct.stream()
                .map(ReporteClienteItemDTO::getTotalGastado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Segunda pasada: asignar porcentaje ahora que tenemos el total
        List<ReporteClienteItemDTO> items = itemsSinPct.stream()
                .map(item -> {
                    item.setPorcentaje(calcularPorcentaje(item.getTotalGastado(), totalPeriodo));
                    return item;
                }).toList();

        BigDecimal ticketPromedio = items.isEmpty() ? BigDecimal.ZERO
                : totalPeriodo.divide(BigDecimal.valueOf(items.size()), 2, RoundingMode.HALF_UP);

        return ReporteClientesDTO.builder()
                .fechaInicio(fechaInicio).fechaFin(fechaFin)
                .totalClientes(items.size())
                .totalVendido(totalPeriodo)
                .ticketPromedio(ticketPromedio)
                .clientes(items)
                .detalleVentas(buildDetalleVentas(inicio, fin))
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // REPORTE 3 — VENTAS POR PRODUCTO
    // ════════════════════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public ReporteProductosDTO reporteVentasPorProducto(LocalDate fechaInicio, LocalDate fechaFin) {

        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin    = fechaFin.atTime(23, 59, 59);

        // Primera pasada: construir items sin porcentaje
        List<ReporteProductoItemDTO> itemsSinPct = productoRepository
                .findProductosMasVendidosPorRango(inicio, fin).stream()
                .map(row -> {
                    Producto p = (Producto) row[0];
                    return ReporteProductoItemDTO.builder()
                            .productoId(p.getId())
                            .nombre(p.getNombre())
                            .marca(p.getMarca())
                            .imagen(p.getImagen())
                            .categoria(p.getCategoria().getNombre())
                            .cantidad(p.getCantidad())
                            .unidadMedida(p.getUnidadMedida())
                            .unidadesVendidas(toLong(row[1]))
                            .ingresos(toBigDecimal(row, 2))
                            .stockActual(p.getStock())
                            .porcentaje(0)
                            .build();
                }).toList();

        BigDecimal totalIngresos = itemsSinPct.stream()
                .map(ReporteProductoItemDTO::getIngresos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalUnidades = itemsSinPct.stream()
                .mapToLong(ReporteProductoItemDTO::getUnidadesVendidas).sum();

        // Segunda pasada: asignar porcentaje
        List<ReporteProductoItemDTO> items = itemsSinPct.stream()
                .map(item -> {
                    item.setPorcentaje(calcularPorcentaje(item.getIngresos(), totalIngresos));
                    return item;
                }).toList();

        return ReporteProductosDTO.builder()
                .fechaInicio(fechaInicio).fechaFin(fechaFin)
                .totalProductosVendidos(items.size())
                .totalUnidades(totalUnidades)
                .totalIngresos(totalIngresos)
                .productos(items)
                .detalleVentas(buildDetalleVentas(inicio, fin))
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // DASHBOARD
    // ════════════════════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public DashboardDTO dashboard() {
        LocalDate hoy      = LocalDate.now();
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia    = hoy.atTime(23, 59, 59);
        LocalDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes    = inicioMes.plusMonths(1).minusSeconds(1);

        // Últimos 7 días: acumular total real por día
        DateTimeFormatter fmtLabel = DateTimeFormatter.ofPattern("EEE d", Locale.of("es", "PE"));
        List<BigDecimal> ventas7   = new ArrayList<>();
        List<String>     labels7   = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate dia        = hoy.minusDays(i);
            LocalDateTime inicio = dia.atStartOfDay();
            LocalDateTime fin    = dia.atTime(23, 59, 59);
            BigDecimal total     = ventaRepository.sumTotalByFechaHoraBetween(inicio, fin);
            ventas7.add(total != null ? total : BigDecimal.ZERO);
            labels7.add(dia.format(fmtLabel));
        }

        return DashboardDTO.builder()
                .totalVentasHoy(ventaRepository.sumTotalByFechaHoraBetween(inicioDia, finDia))
                .cantidadVentasHoy(ventaRepository.countVentasByFechaHoraBetween(inicioDia, finDia))
                .totalVentasMes(ventaRepository.sumTotalByFechaHoraBetween(inicioMes, finMes))
                .cantidadVentasMes(ventaRepository.countVentasByFechaHoraBetween(inicioMes, finMes))
                .productosConStockBajo(productoRepository.findProductosConStockBajo().size())
                .productosSinStock(productoRepository.findProductosSinStock().size())
                .ordenesPendientes((int) ordenCompraRepository.countByEstado(EstadoOrden.PENDIENTE))
                .ventasUltimos7Dias(ventas7)
                .labelsUltimos7Dias(labels7)
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Convierte cualquier elemento de Object[] a BigDecimal de forma segura. */
    private BigDecimal toBigDecimal(Object[] arr, int idx) {
        if (arr == null || idx < 0 || idx >= arr.length || arr[idx] == null) return BigDecimal.ZERO;
        Object v = arr[idx];
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return new BigDecimal(n.toString());
        return BigDecimal.ZERO;
    }

    /** COUNT() puede devolver Integer o Long según el proveedor JPA — cast seguro. */
    private Long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Long l) return l;
        if (v instanceof Number n) return n.longValue();
        return 0L;
    }

    /** CAST(... AS date) puede devolver LocalDate, java.sql.Date o Timestamp según el proveedor JPA. */
    private LocalDate toLocalDate(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDate ld) return ld;
        if (v instanceof java.sql.Date sd) return sd.toLocalDate();
        if (v instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        if (v instanceof LocalDateTime ldt) return ldt.toLocalDate();
        if (v instanceof java.util.Date d) return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return null;
    }

    /** Porcentaje entero de parcial respecto al total; 0 si total es 0. */
    private int calcularPorcentaje(BigDecimal parcial, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return 0;
        return parcial.multiply(BigDecimal.valueOf(100))
                .divide(total, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    /** Lista de ventas del período con su fecha/hora, para el detalle de los Excel. */
    private List<VentaDetalleDTO> buildDetalleVentas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findVentasDetalleEnRango(inicio, fin).stream()
                .map(v -> VentaDetalleDTO.builder()
                        .fechaHora(v.getFechaHora())
                        .numeroComprobante(v.getNumeroComprobante())
                        .tipoComprobante(v.getTipoComprobante().name())
                        .cliente(v.getCliente() != null
                                ? v.getCliente().getNombre() + " " + v.getCliente().getApellido()
                                : "Público general")
                        .metodoPago(v.getMetodoPago().name())
                        .subtotal(v.getSubtotal())
                        .igv(v.getIgv())
                        .total(v.getTotal())
                        .build())
                .toList();
    }


    // ════════════════════════════════════════════════════════════════════════
    // DTOs INTERNOS — solo usados por ReporteService
    // ════════════════════════════════════════════════════════════════════════

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ReporteVentasMesDTO {
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private Long cantidadVentas;
        private BigDecimal subtotal;
        private BigDecimal igv;
        private BigDecimal total;
        private BigDecimal ticketPromedio;
        private List<ResumenComprobanteDTO> porTipoComprobante;
        private List<ResumenMetodoPagoDTO>  porMetodoPago;
        private List<ResumenDiaDTO>         porDia;
        private List<VentaDetalleDTO>       detalleVentas;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VentaDetalleDTO {
        private LocalDateTime fechaHora;
        private String numeroComprobante;
        private String tipoComprobante;
        private String cliente;
        private String metodoPago;
        private BigDecimal subtotal;
        private BigDecimal igv;
        private BigDecimal total;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ResumenComprobanteDTO {
        private String tipoComprobante;
        private Long cantidadVentas;
        private BigDecimal total;
        private int porcentaje;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ResumenMetodoPagoDTO {
        private String metodoPago;
        private Long cantidadVentas;
        private BigDecimal total;
        private int porcentaje;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ResumenDiaDTO {
        private LocalDate fecha;
        private Long cantidadVentas;
        private BigDecimal total;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ReporteClientesDTO {
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private int totalClientes;
        private BigDecimal totalVendido;
        private BigDecimal ticketPromedio;
        private List<ReporteClienteItemDTO> clientes;
        private List<VentaDetalleDTO> detalleVentas;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ReporteClienteItemDTO {
        private Long clienteId;
        private String nombreCompleto;
        private String tipoDocumento;
        private String numeroDocumento;
        private Long cantidadCompras;
        private BigDecimal totalGastado;
        private int porcentaje;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ReporteProductosDTO {
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private int totalProductosVendidos;
        private Long totalUnidades;
        private BigDecimal totalIngresos;
        private List<ReporteProductoItemDTO> productos;
        private List<VentaDetalleDTO> detalleVentas;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ReporteProductoItemDTO {
        private Long productoId;
        private String nombre;
        private String marca;
        private String imagen;
        private String categoria;
        private Double cantidad;
        private String unidadMedida;
        private Long unidadesVendidas;
        private BigDecimal ingresos;
        private Integer stockActual;
        private int porcentaje;
    }
}
