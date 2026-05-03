package com.tulicoreria.licoreria.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ejemplos: Whisky, Ron, Cerveza, Vino, Vodka, Pisco, Gin, Aguardiente
     */
    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Producto> productos = new ArrayList<>();
}
