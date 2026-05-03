package com.tulicoreria.licoreria.repository;

import com.tulicoreria.licoreria.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Solo categorías activas para combos en formularios
    List<Categoria> findByActivoTrue();

    // Verificar duplicado antes de crear
    boolean existsByNombre(String nombre);
}
