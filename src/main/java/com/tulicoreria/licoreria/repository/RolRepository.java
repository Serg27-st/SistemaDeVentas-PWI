package com.tulicoreria.licoreria.repository;

import com.tulicoreria.licoreria.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    // Spring Security lo necesita para asignar roles al registrar usuarios
    Optional<Rol> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
