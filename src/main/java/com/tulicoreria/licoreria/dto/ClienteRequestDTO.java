package com.tulicoreria.licoreria.dto;

import com.tulicoreria.licoreria.model.Cliente.TipoDocumento;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequestDTO {

    private String nombre;
    private String apellido;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;

    // Formato ISO (yyyy-MM-dd) — el que envía el <input type="date">
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    private String telefono;
    private String correo;
    private String direccion;
}
