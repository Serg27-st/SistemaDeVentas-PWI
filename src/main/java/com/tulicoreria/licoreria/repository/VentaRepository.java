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

    // ── Reporte por Mes ──────────────────────────────────────────────────────

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

    // Detalle de ventas de un mes específico con totales por tipo de comprobante:
    // devuelve [tipoComprobante, cantVentas, totalVendido]
    @Query("SELECT v.tipoComprobante, COUNT(v), SUM(v.total) " +
           "FROM Venta v " +
           "WHERE v.estado = 'COMPLETADA' " +
           "AND MONTH(v.fechaHora) = :mes " +
           "AND YEAR(v.fechaHora) = :anio " +
           "GROUP BY v.tipoComprobante")
    List<Object[]> findResumenPorTipoComprobante(
            @Param("mes") int mes,
            @Param("anio") int anio
    );

    // Totales por método de pago en un mes:
    // devuelve [metodoPago, cantVentas, totalVendido]
    @Query("SELECT v.metodoPago, COUNT(v), SUM(v.total) " +
           "FROM Venta v " +
           "WHERE v.estado = 'COMPLETADA' " +
           "AND MONTH(v.fechaHora) = :mes " +
           "AND YEAR(v.fechaHora) = :anio " +
           "GROUP BY v.metodoPago")
    List<Object[]> findResumenPorMetodoPago(
            @Param("mes") int mes,
            @Param("anio") int anio
    );

    // Total, IGV y subtotal del mes para declaración tributaria.
    // Retorna List<Object[]> con exactamente 1 fila: [subtotal, igv, total].
    @Query("SELECT COALESCE(SUM(v.subtotal), 0), " +
           "COALESCE(SUM(v.igv), 0), " +
           "COALESCE(SUM(v.total), 0) " +
           "FROM Venta v " +
           "WHERE v.estado = 'COMPLETADA' " +
           "AND MONTH(v.fechaHora) = :mes " +
           "AND YEAR(v.fechaHora)  = :anio")
    List<Object[]> findTotalesPorMes(
            @Param("mes") int mes,
            @Param("anio") int anio);

    // Desglose diario del mes: [dia(int), cantVentas(Long), total(BigDecimal)]
    @Query("SELECT DAY(v.fechaHora), COUNT(v), SUM(v.total) " +
           "FROM Venta v " +
           "WHERE v.estado = 'COMPLETADA' " +
           "AND MONTH(v.fechaHora) = :mes " +
           "AND YEAR(v.fechaHora)  = :anio " +
           "GROUP BY DAY(v.fechaHora) " +
           "ORDER BY DAY(v.fechaHora)")
    List<Object[]> findResumenPorDia(
            @Param("mes") int mes,
            @Param("anio") int anio);
}
