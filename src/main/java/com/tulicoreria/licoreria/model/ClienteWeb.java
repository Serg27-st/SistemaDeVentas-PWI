package com.tulicoreria.licoreria.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes_web")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteWeb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(length = 20)
    private String telefono;

    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    /** Referencia al cliente de negocio para vincular ventas del carrito */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToMany(mappedBy = "clienteWeb", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DireccionEnvio> direcciones = new ArrayList<>();

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
