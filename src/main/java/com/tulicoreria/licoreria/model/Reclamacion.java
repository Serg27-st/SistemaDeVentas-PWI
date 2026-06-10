package com.tulicoreria.licoreria.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reclamaciones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reclamacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoReclamo tipoReclamo;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(length = 200)
    private String productoAfectado;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReclamo estado;

    public enum TipoReclamo {
        QUEJA, RECLAMO
    }

    public enum EstadoReclamo {
        PENDIENTE, ATENDIDO
    }
}
