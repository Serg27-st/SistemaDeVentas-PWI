package com.tulicoreria.licoreria.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Convención de Spring Security: prefijo ROLE_
     * Valores esperados: ROLE_ADMIN, ROLE_VENDEDOR, ROLE_ALMACEN
     */
    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(length = 200)
    private String descripcion;
}
