package com.tulicoreria.licoreria.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tulicoreria.licoreria.model.Kardex;
import com.tulicoreria.licoreria.model.Kardex.TipoMovimiento;
import com.tulicoreria.licoreria.model.Producto;

@Repository
public interface KardexRepository extends JpaRepository<Kardex, Long> {

    // Historial completo de un producto ordenado por fecha (vista de ficha)
    List<Kardex> findByProductoOrderByFechaHoraDesc(Producto producto);

    // Últimos 10 movimientos de un producto (resumen rápido)
    List<Kardex> findTop10ByProductoOrderByFechaHoraDesc(Producto producto);

    // Movimientos por tipo para análisis (ej: solo mermas del mes)
    List<Kardex> findByProductoAndTipo(Producto producto, TipoMovimiento tipo);

    // Movimientos en un rango de fechas para reportes de inventario
    List<Kardex> findByFechaHoraBetweenOrderByFechaHoraDesc(
            LocalDateTime inicio,
            LocalDateTime fin
    );
}
