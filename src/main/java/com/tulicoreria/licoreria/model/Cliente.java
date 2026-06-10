package com.tulicoreria.licoreria.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    /**
     * Tipo de documento: DNI, CE, PASAPORTE
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDocumento tipoDocumento;

    @Column(nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    /**
     * Requerido para verificar mayoría de edad (18+) al vender alcohol
     */
    private LocalDate fechaNacimiento;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String correo;

    @Column(length = 200)
    private String direccion;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Venta> ventas = new ArrayList<>();

    public enum TipoDocumento {
        DNI, CE, PASAPORTE
    }

    /**
     * Verifica si el cliente tiene 18 años o más
     */
    public boolean esMayorDeEdad() {
        if (fechaNacimiento == null) return false;
        return LocalDate.now().minusYears(18).isAfter(fechaNacimiento)
               || LocalDate.now().minusYears(18).isEqual(fechaNacimiento);
    }
}
