package com.tulicoreria.licoreria.repository;

import com.tulicoreria.licoreria.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Buscar por número de documento en el punto de venta
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    // Buscador por nombre o apellido
    List<Cliente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
            String nombre, String apellido
    );

    boolean existsByNumeroDocumento(String numeroDocumento);

    // ── Reporte por Cliente ──────────────────────────────────────────────────

    // Clientes con más compras en un mes: devuelve [Cliente, cantVentas, totalGastado]
    // Usado en ReporteService para el reporte mensual por cliente
    @Query("SELECT v.cliente, COUNT(v), SUM(v.total) " +
           "FROM Venta v " +
           "WHERE v.estado = 'COMPLETADA' " +
           "AND MONTH(v.fechaHora) = :mes " +
           "AND YEAR(v.fechaHora) = :anio " +
           "AND v.cliente IS NOT NULL " +
           "GROUP BY v.cliente " +
           "ORDER BY SUM(v.total) DESC")
    List<Object[]> findClientesMasCompradoresPorMes(
            @Param("mes") int mes,
            @Param("anio") int anio
    );

    // Clientes con más compras en un rango de fechas libre
    @Query("SELECT v.cliente, COUNT(v), SUM(v.total) " +
           "FROM Venta v " +
           "WHERE v.estado = 'COMPLETADA' " +
           "AND v.fechaHora BETWEEN :inicio AND :fin " +
           "AND v.cliente IS NOT NULL " +
           "GROUP BY v.cliente " +
           "ORDER BY SUM(v.total) DESC")
    List<Object[]> findClientesMasCompradoresPorRango(
            @Param("inicio") java.time.LocalDateTime inicio,
            @Param("fin") java.time.LocalDateTime fin
    );
}
