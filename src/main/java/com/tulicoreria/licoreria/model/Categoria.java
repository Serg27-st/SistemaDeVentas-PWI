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

    // 🌟 CORREGIDO: Evita bucles infinitos en serialización y asegura la carga limpia
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    @ToString.Exclude // 🔑 Evita que un toString() accidental rompa el hilo al llamar a la lista
    private List<Producto> productos = new ArrayList<>();

    /**
     * 🛠️ MÉTODO HELPER (Buenas prácticas JPA): Sincroniza ambos lados de la
     * relación para evitar que la lista devuelva NullPointer o se quede colgada
     * esperando al revés.
     */
    public void addProducto(Producto producto) {
        if (this.productos == null) {
            this.productos = new ArrayList<>();
        }
        this.productos.add(producto);
        producto.setCategoria(this);
    }
}
