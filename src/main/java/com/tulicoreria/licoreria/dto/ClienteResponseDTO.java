 package com.tulicoreria.licoreria.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

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

    // Formato ISO (yyyy-MM-dd) para que th:field lo renderice correctamente
    // en el <input type="date"> del formulario de edición.
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    private String telefono;
    private String correo;
    private String direccion;

    // Calculado con esMayorDeEdad() del modelo — importante para ventas de alcohol
    private boolean mayorDeEdad;

    // Total de compras realizadas
    private int totalVentas;
}
