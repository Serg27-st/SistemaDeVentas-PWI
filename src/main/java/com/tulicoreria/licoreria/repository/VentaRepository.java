package com.tulicoreria.licoreria.repository;

import com.tulicoreria.licoreria.model.Usuario;
import com.tulicoreria.licoreria.model.Venta;
import com.tulicoreria.licoreria.model.Venta.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Buscar por número de comprobante
    Optional<Venta> findByNumeroComprobante(String numeroComprobante);

    // Ventas en un rango de fechas con estado específico (cierre de caja)
    List<Venta> findByFechaHoraBetweenAndEstado(
            LocalDateTime inicio,
            LocalDateTime fin,
            EstadoVenta estado
    );

    // Ventas por vendedor para control de desempeño
    List<Venta> findByVendedor(Usuario vendedor);

    // Historial de pedidos de un cliente registrado (tienda web)
    List<Venta> findByClienteIdOrderByFechaHoraDesc(Long clienteId);

    // Último número de comprobante para generar el siguiente (BOLETA-000002)
    @Query("SELECT MAX(v.numeroComprobante) FROM Venta v " +
           "WHERE v.tipoComprobante = :tipo")
    String findUltimoNumeroComprobante(
            @Param("tipo") Venta.TipoComprobante tipo
    );

    // ── Dashboard ────────────────────────────────────────────────────────────

    // Total vendido en un rango de fechas
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v " +
           "WHERE v.fechaHora BETWEEN :inicio AND :fin " +
           "AND v.estado = 'COMPLETADA'")
    BigDecimal sumTotalByFechaHoraBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Cantidad de ventas en un rango de fechas
    @Query("SELECT COUNT(v) FROM Venta v " +
           "WHERE v.fechaHora BETWEEN :inicio AND :fin " +
           "AND v.estado = 'COMPLETADA'")
    Long countVentasByFechaHoraBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // ── Reporte por Período ──────────────────────────────────────────────────

    // Resumen de ventas agrupado por mes y año:
    // devuelve [anio, mes, cantVentas, totalVendido]
    // Usado en ReporteService para el historial mensual
    @Query("SELECT YEAR(v.fechaHora), MONTH(v.fechaHora), " +
           "COUNT(v), SUM(v.total) " +
           "FROM Venta v " +
           "WHERE v.estado = 'COMPLETADA' " +
           "GROUP BY YEAR(v.fechaHora), MONTH(v.fechaHora) " +
           "ORDER BY YEAR(v.fechaHora) DESC, MONTH(v.fechaHora) DESC")
    List<Object[]> findResumenPorMes();

    // Detalle de ventas de un período con totales por tipo de comprobante:
    // devuelve [tipoComprobante, cantVentas, totalVendido]
    @Query("SELECT v.tipoComprobante, COUNT(v), SUM(v.total) " +
           "FROM Venta v " +
           "WHERE v.estado = 'COMPLETADA' " +
           "AND v.fechaHora BETWEEN :inicio AND :fin " +
           "GROUP BY v.tipoComprobante")
    List<Object[]> findResumenPorTipoComprobanteEnRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Totales por método de pago en un período:
    // devuelve [metodoPago, cantVentas, totalVendido]
    @Query("SELECT v.metodoPago, COUNT(v), SUM(v.total) " +
           "FROM Venta v " +
           "WHERE v.estado = 'COMPLETADA' " +
           "AND v.fechaHora BETWEEN :inicio AND :fin " +
           "GROUP BY v.metodoPago")
    List<Object[]> findResumenPorMetodoPagoEnRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Total, IGV y subtotal del período para declaración tributaria.
    // Retorna List<Object[]> con exactamente 1 fila: [subtotal, igv, total].
    @Query("SELECT COALESCE(SUM(v.subtotal), 0), " +
           "COALESCE(SUM(v.igv), 0), " +
           "COALESCE(SUM(v.total), 0) " +
           "FROM Venta v " +
           "WHERE v.estado = 'COMPLETADA' " +
           "AND v.fechaHora BETWEEN :inicio AND :fin")
    List<Object[]> findTotalesPorRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    // Desglose diario del período: [fecha(date), cantVentas(Long), total(BigDecimal)]
    @Query("SELECT CAST(v.fechaHora AS date), COUNT(v), SUM(v.total) " +
           "FROM Venta v " +
           "WHERE v.estado = 'COMPLETADA' " +
           "AND v.fechaHora BETWEEN :inicio AND :fin " +
           "GROUP BY CAST(v.fechaHora AS date) " +
           "ORDER BY CAST(v.fechaHora AS date)")
    List<Object[]> findResumenPorDiaEnRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    // Ventas COMPLETADA del período con su cliente cargado, ordenadas por fecha.
    // Usada para el detalle de ventas (con fecha y hora) de los reportes Excel.
    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.cliente " +
           "WHERE v.estado = 'COMPLETADA' " +
           "AND v.fechaHora BETWEEN :inicio AND :fin " +
           "ORDER BY v.fechaHora")
    List<Venta> findVentasDetalleEnRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}
