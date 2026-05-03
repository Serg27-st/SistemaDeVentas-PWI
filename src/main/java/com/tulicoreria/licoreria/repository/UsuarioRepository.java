package com.tulicoreria.licoreria.repository;

import com.tulicoreria.licoreria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring Security lo necesita para el login
    Optional<Usuario> findByUsername(String username);

    // Verificar duplicados antes de registrar
    boolean existsByUsername(String username);
    boolean existsByCorreo(String correo);

    // Solo usuarios activos para el módulo de administración
    List<Usuario> findByActivoTrue();
}
