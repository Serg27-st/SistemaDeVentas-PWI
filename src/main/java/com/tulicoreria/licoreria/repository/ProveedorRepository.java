package com.tulicoreria.licoreria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tulicoreria.licoreria.model.Proveedor;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    // Solo proveedores activos para el formulario de orden de compra
    List<Proveedor> findByActivoTrue();

    // Buscar por RUC para evitar duplicados al registrar
    Optional<Proveedor> findByRuc(String ruc);

    boolean existsByRuc(String ruc);
}
