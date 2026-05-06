package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.dto.DashboardDTO;
import com.tulicoreria.licoreria.model.Cliente;
import com.tulicoreria.licoreria.model.OrdenCompra.EstadoOrden;
import com.tulicoreria.licoreria.model.Producto;
import com.tulicoreria.licoreria.repository.*;
import com.tulicoreria.licoreria.service.ReporteService;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
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
    // REPORTE 1 — VENTAS POR MES
    // ════════════════════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public ReporteVentasMesDTO reporteVentasPorMes(int mes, int anio) {

        Object[] totales  = ventaRepository.findTotalesPorMes(mes, anio);
        BigDecimal subtotal = totales[0] != null ? (BigDecimal) totales[0] : BigDecimal.ZERO;
        BigDecimal igv      = totales[1] != null ? (BigDecimal) totales[1] : BigDecimal.ZERO;
        BigDecimal total    = totales[2] != null ? (BigDecimal) totales[2] : BigDecimal.ZERO;

        LocalDateTime inicio = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime fin    = inicio.plusMonths(1).minusSeconds(1);
        Long cantidadVentas  = ventaRepository.countVentasByFechaHoraBetween(inicio, fin);

        BigDecimal ticketPromedio = cantidadVentas > 0
                ? total.divide(BigDecimal.valueOf(cantidadVentas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<ResumenComprobanteDTO> comprobantes = ventaRepository
                .findResumenPorTipoComprobante(mes, anio).stream()
                .map(row -> ResumenComprobanteDTO.builder()
                        .tipoComprobante(row[0].toString())
                        .cantidadVentas((Long) row[1])
                        .total((BigDecimal) row[2])
                        .build())
                .toList();

        List<ResumenMetodoPagoDTO> metodosPago = ventaRepository
                .findResumenPorMetodoPago(mes, anio).stream()
                .map(row -> ResumenMetodoPagoDTO.builder()
                        .metodoPago(row[0].toString())
                        .cantidadVentas((Long) row[1])
                        .total((BigDecimal) row[2])
                        .build())
                .toList();

        return ReporteVentasMesDTO.builder()
                .mes(mes).anio(anio)
                .nombreMes(nombreMes(mes))
                .cantidadVentas(cantidadVentas)
                .subtotal(subtotal).igv(igv).total(total)
                .ticketPromedio(ticketPromedio)
                .porTipoComprobante(comprobantes)
                .porMetodoPago(metodosPago)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // REPORTE 2 — VENTAS POR CLIENTE
    // ════════════════════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public ReporteClientesDTO reporteVentasPorCliente(int mes, int anio) {

        List<ReporteClienteItemDTO> items = clienteRepository
                .findClientesMasCompradoresPorMes(mes, anio).stream()
                .map(row -> {
                    Cliente c = (Cliente) row[0];
                    return ReporteClienteItemDTO.builder()
                            .clienteId(c.getId())
                            .nombreCompleto(c.getNombre() + " " + c.getApellido())
                            .tipoDocumento(c.getTipoDocumento().name())
                            .numeroDocumento(c.getNumeroDocumento())
                            .cantidadCompras((Long) row[1])
                            .totalGastado((BigDecimal) row[2])
                            .build();
                }).toList();

        BigDecimal totalMes = items.stream()
                .map(ReporteClienteItemDTO::getTotalGastado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ReporteClientesDTO.builder()
                .mes(mes).anio(anio)
                .nombreMes(nombreMes(mes))
                .totalClientes(items.size())
                .totalVendido(totalMes)
                .clientes(items)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // REPORTE 3 — VENTAS POR PRODUCTO
    // ════════════════════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public ReporteProductosDTO reporteVentasPorProducto(int mes, int anio) {

        List<ReporteProductoItemDTO> items = productoRepository
                .findProductosMasVendidosPorMes(mes, anio).stream()
                .map(row -> {
                    Producto p = (Producto) row[0];
                    return ReporteProductoItemDTO.builder()
                            .productoId(p.getId())
                            .nombre(p.getNombre())
                            .marca(p.getMarca())
                            .categoria(p.getCategoria().getNombre())
                            .volumenMl(p.getVolumenMl())
                            .unidadesVendidas((Long) row[1])
                            .ingresos((BigDecimal) row[2])
                            .stockActual(p.getStock())
                            .build();
                }).toList();

        BigDecimal totalIngresos = items.stream()
                .map(ReporteProductoItemDTO::getIngresos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalUnidades = items.stream()
                .mapToLong(ReporteProductoItemDTO::getUnidadesVendidas).sum();

        return ReporteProductosDTO.builder()
                .mes(mes).anio(anio)
                .nombreMes(nombreMes(mes))
                .totalProductosVendidos(items.size())
                .totalUnidades(totalUnidades)
                .totalIngresos(totalIngresos)
                .productos(items)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // DASHBOARD
    // ════════════════════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public DashboardDTO dashboard() {
        LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finDia    = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime finMes    = inicioMes.plusMonths(1).minusSeconds(1);

        return DashboardDTO.builder()
                .totalVentasHoy(ventaRepository.sumTotalByFechaHoraBetween(inicioDia, finDia))
                .cantidadVentasHoy(ventaRepository.countVentasByFechaHoraBetween(inicioDia, finDia))
                .totalVentasMes(ventaRepository.sumTotalByFechaHoraBetween(inicioMes, finMes))
                .cantidadVentasMes(ventaRepository.countVentasByFechaHoraBetween(inicioMes, finMes))
                .productosConStockBajo(productoRepository.findProductosConStockBajo().size())
                .productosSinStock(productoRepository.findProductosSinStock().size())
                .ordenesPendientes((int) ordenCompraRepository.countByEstado(EstadoOrden.PENDIENTE))
                .build();
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private String nombreMes(int mes) {
        return Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("es", "PE"));
    }

    // ════════════════════════════════════════════════════════════════════════
    // DTOs INTERNOS — solo usados por ReporteService
    // ════════════════════════════════════════════════════════════════════════

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ReporteVentasMesDTO {
        private int mes;
        private int anio;
        private String nombreMes;
        private Long cantidadVentas;
        private BigDecimal subtotal;
        private BigDecimal igv;
        private BigDecimal total;
        private BigDecimal ticketPromedio;
        private List<ResumenComprobanteDTO> porTipoComprobante;
        private List<ResumenMetodoPagoDTO> porMetodoPago;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ResumenComprobanteDTO {
        private String tipoComprobante;
        private Long cantidadVentas;
        private BigDecimal total;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ResumenMetodoPagoDTO {
        private String metodoPago;
        private Long cantidadVentas;
        private BigDecimal total;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ReporteClientesDTO {
        private int mes;
        private int anio;
        private String nombreMes;
        private int totalClientes;
        private BigDecimal totalVendido;
        private List<ReporteClienteItemDTO> clientes;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ReporteClienteItemDTO {
        private Long clienteId;
        private String nombreCompleto;
        private String tipoDocumento;
        private String numeroDocumento;
        private Long cantidadCompras;
        private BigDecimal totalGastado;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ReporteProductosDTO {
        private int mes;
        private int anio;
        private String nombreMes;
        private int totalProductosVendidos;
        private Long totalUnidades;
        private BigDecimal totalIngresos;
        private List<ReporteProductoItemDTO> productos;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ReporteProductoItemDTO {
        private Long productoId;
        private String nombre;
        private String marca;
        private String categoria;
        private Integer volumenMl;
        private Long unidadesVendidas;
        private BigDecimal ingresos;
        private Integer stockActual;
    }
}
