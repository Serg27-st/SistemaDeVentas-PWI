package com.tulicoreria.licoreria.repository;

import com.tulicoreria.licoreria.model.DireccionEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DireccionEnvioRepository extends JpaRepository<DireccionEnvio, Long> {
    List<DireccionEnvio> findByClienteWebId(Long clienteWebId);
}
