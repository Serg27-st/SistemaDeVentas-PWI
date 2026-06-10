package com.tulicoreria.licoreria.repository;

import com.tulicoreria.licoreria.model.BusquedaLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusquedaLogRepository extends JpaRepository<BusquedaLog, Long> {

    Optional<BusquedaLog> findByTermino(String termino);

    /** Top N términos más buscados para la sección de tendencias. */
    List<BusquedaLog> findTop6ByOrderByConteoDesc();
}
