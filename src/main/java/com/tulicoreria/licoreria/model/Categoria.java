package com.tulicoreria.licoreria.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(length = 300)
    private String descripcion;


    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    // Evita bucles infinitos en la serialización JSON del lado "muchos" de la relación
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    @ToString.Exclude // Evita que un toString() accidental dispare la carga perezosa de la lista
    private List<Producto> productos = new ArrayList<>();
}
