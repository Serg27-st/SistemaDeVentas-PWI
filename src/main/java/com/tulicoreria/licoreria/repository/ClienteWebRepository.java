package com.tulicoreria.licoreria.repository;

import com.tulicoreria.licoreria.model.ClienteWeb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteWebRepository extends JpaRepository<ClienteWeb, Long> {
    Optional<ClienteWeb> findByEmail(String email);
    boolean existsByEmail(String email);
}
