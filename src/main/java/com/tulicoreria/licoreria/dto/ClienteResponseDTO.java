 package com.tulicoreria.licoreria.dto;

import java.time.LocalDate;

import com.tulicoreria.licoreria.model.Cliente.TipoDocumento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String nombreCompleto;       // nombre + apellido para mostrar en vista
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String correo;
    private String direccion;

    // Calculado con esMayorDeEdad() del modelo — importante para ventas de alcohol
    private boolean mayorDeEdad;

    // Total de compras realizadas
    private int totalVentas;
}
