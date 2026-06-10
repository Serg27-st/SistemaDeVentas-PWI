package com.tulicoreria.licoreria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tulicoreria.licoreria.model.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    // Spring Security lo necesita para asignar roles al registrar usuarios
    Optional<Rol> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
