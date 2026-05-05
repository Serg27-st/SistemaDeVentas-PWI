package com.tulicoreria.licoreria.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tulicoreria.licoreria.model.Usuario;
import com.tulicoreria.licoreria.model.Venta;
import com.tulicoreria.licoreria.model.Venta.EstadoVenta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Buscar por número de comprobante (BOLETA-000001)
    Optional<Venta> findByNumeroComprobante(String numeroComprobante);

    // Ventas del día para el cierre de caja
    List<Venta> findByFechaHoraBetweenAndEstado(
            LocalDateTime inicio,
            LocalDateTime fin,
            EstadoVenta estado
    );

    // Ventas por vendedor para control de desempeño
    List<Venta> findByVendedor(Usuario vendedor);

    // Total vendido en un rango de fechas (para dashboard)
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v "
            + "WHERE v.fechaHora BETWEEN :inicio AND :fin "
            + "AND v.estado = 'COMPLETADA'")
    BigDecimal sumTotalByFechaHoraBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Cantidad de ventas del día
    @Query("SELECT COUNT(v) FROM Venta v "
            + "WHERE v.fechaHora BETWEEN :inicio AND :fin "
            + "AND v.estado = 'COMPLETADA'")
    Long countVentasDelDia(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Ventas agrupadas por mes y año
    @Query("SELECT MONTH(v.fechaHora), YEAR(v.fechaHora), "
            + "COUNT(v), SUM(v.total) FROM Venta v "
            + "WHERE v.estado = 'COMPLETADA' "
            + "GROUP BY YEAR(v.fechaHora), MONTH(v.fechaHora) "
            + "ORDER BY YEAR(v.fechaHora) DESC, MONTH(v.fechaHora) DESC")
    List<Object[]> findResumenPorMes();

// Ventas por cliente en un mes específico
    @Query("SELECT v.cliente, COUNT(v), SUM(v.total) FROM Venta v "
            + "WHERE v.estado = 'COMPLETADA' "
            + "AND MONTH(v.fechaHora) = :mes "
            + "AND YEAR(v.fechaHora) = :anio "
            + "AND v.cliente IS NOT NULL "
            + "GROUP BY v.cliente ORDER BY SUM(v.total) DESC")
    List<Object[]> findVentasPorCliente(
            @Param("mes") int mes,
            @Param("anio") int anio
    );

    // Último número de comprobante para generar el siguiente
    @Query("SELECT MAX(v.numeroComprobante) FROM Venta v WHERE v.tipoComprobante = :tipo")
    String findUltimoNumeroComprobante(@Param("tipo") Venta.TipoComprobante tipo);
}
