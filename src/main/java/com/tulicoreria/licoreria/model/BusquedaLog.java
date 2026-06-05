package com.tulicoreria.licoreria.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Registro de búsquedas para calcular tendencias (lo más buscado).
 */
@Entity
@Table(name = "busqueda_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BusquedaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String termino;

    @Column(nullable = false)
    private long conteo;

    @Column(nullable = false)
    private LocalDateTime ultimaBusqueda;
}
