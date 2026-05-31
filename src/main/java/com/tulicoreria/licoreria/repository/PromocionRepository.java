package com.tulicoreria.licoreria.repository;

import com.tulicoreria.licoreria.model.Promocion;
import com.tulicoreria.licoreria.model.Promocion.TipoPromocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    @Query("""
        SELECT p FROM Promocion p
        WHERE p.tipo = :tipo
          AND p.activo = true
          AND p.producto.id = :productoId
          AND (p.fechaInicio IS NULL OR p.fechaInicio <= :hoy)
          AND (p.fechaFin   IS NULL OR p.fechaFin   >= :hoy)
        ORDER BY p.id DESC
        """)
    Optional<Promocion> findActivaPorTipoYProducto(
            @Param("tipo")       TipoPromocion tipo,
            @Param("productoId") Long productoId,
            @Param("hoy")        LocalDate hoy);

    @Query("""
        SELECT p FROM Promocion p
        WHERE p.tipo = 'COMBO'
          AND p.activo = true
          AND (p.fechaInicio IS NULL OR p.fechaInicio <= :hoy)
          AND (p.fechaFin   IS NULL OR p.fechaFin   >= :hoy)
        ORDER BY p.id DESC
        """)
    List<Promocion> findCombosActivos(@Param("hoy") LocalDate hoy);

    /** Todas las promociones activas hoy (para enriquecer el catálogo en batch). */
    @Query("""
        SELECT p FROM Promocion p
        WHERE p.activo = true
          AND (p.fechaInicio IS NULL OR p.fechaInicio <= :hoy)
          AND (p.fechaFin   IS NULL OR p.fechaFin   >= :hoy)
        """)
    List<Promocion> findTodasActivas(@Param("hoy") LocalDate hoy);

    List<Promocion> findAllByOrderByIdDesc();
}
