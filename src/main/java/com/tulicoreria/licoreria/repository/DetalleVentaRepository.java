package com.tulicoreria.licoreria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tulicoreria.licoreria.model.DetalleVenta;
import com.tulicoreria.licoreria.model.Producto;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    // Total de unidades vendidas de un producto específico (ficha del producto)
    @Query("SELECT COALESCE(SUM(d.cantidad), 0) FROM DetalleVenta d " +
           "JOIN d.venta v " +
           "WHERE d.producto = :producto " +
           "AND v.estado = 'COMPLETADA'")
    Integer sumCantidadByProducto(@Param("producto") Producto producto);

    // Total de ingresos generados por un producto específico
    @Query("SELECT COALESCE(SUM(d.subtotal), 0) FROM DetalleVenta d " +
           "JOIN d.venta v " +
           "WHERE d.producto = :producto " +
           "AND v.estado = 'COMPLETADA'")
    java.math.BigDecimal sumSubtotalByProducto(@Param("producto") Producto producto);
}
