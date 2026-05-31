package com.tulicoreria.licoreria.repository;

import com.tulicoreria.licoreria.model.Reclamacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReclamacionRepository extends JpaRepository<Reclamacion, Long> {

    List<Reclamacion> findAllByOrderByFechaRegistroDesc();

    long countByEstado(Reclamacion.EstadoReclamo estado);
}
