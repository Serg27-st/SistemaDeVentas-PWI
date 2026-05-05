package com.tulicoreria.licoreria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tulicoreria.licoreria.model.Categoria;
import com.tulicoreria.licoreria.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Buscar por código de barras en el punto de venta
    Optional<Producto> findByCodigo(String codigo);

    // Solo productos activos para catálogo y ventas
    List<Producto> findByActivoTrue();

    // Filtrar por categoría (Whisky, Ron, Cerveza, etc.)
    List<Producto> findByCategoriaAndActivoTrue(Categoria categoria);

    // Buscador en el punto de venta por nombre
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    // Verificar duplicado de código al registrar
    boolean existsByCodigo(String codigo);

    // ── Alertas de inventario ────────────────────────────────────────────────

    // Productos con stock bajo (stock <= stockMinimo) para alertas en dashboard
    @Query("SELECT p FROM Producto p " +
           "WHERE p.stock <= p.stockMinimo AND p.activo = true")
    List<Producto> findProductosConStockBajo();

    // Productos sin stock para reporte de quiebres
    @Query("SELECT p FROM Producto p " +
           "WHERE p.stock = 0 AND p.activo = true")
    List<Producto> findProductosSinStock();

    // ── Reporte por Producto ─────────────────────────────────────────────────

    // Productos más vendidos en un mes: devuelve [Producto, unidades, ingresos]
    // Usado en ReporteService para el reporte mensual por producto
    @Query("SELECT d.producto, SUM(d.cantidad), SUM(d.subtotal) " +
           "FROM DetalleVenta d JOIN d.venta v " +
           "WHERE v.estado = 'COMPLETADA' " +
           "AND MONTH(v.fechaHora) = :mes " +
           "AND YEAR(v.fechaHora) = :anio " +
           "GROUP BY d.producto " +
           "ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findProductosMasVendidosPorMes(
            @Param("mes") int mes,
            @Param("anio") int anio
    );

    // Productos más vendidos en un rango de fechas libre
    @Query("SELECT d.producto, SUM(d.cantidad), SUM(d.subtotal) " +
           "FROM DetalleVenta d JOIN d.venta v " +
           "WHERE v.estado = 'COMPLETADA' " +
           "AND v.fechaHora BETWEEN :inicio AND :fin " +
           "GROUP BY d.producto " +
           "ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findProductosMasVendidosPorRango(
            @Param("inicio") java.time.LocalDateTime inicio,
            @Param("fin") java.time.LocalDateTime fin
    );
}
