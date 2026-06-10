package com.tulicoreria.licoreria.dto;

import com.tulicoreria.licoreria.model.Cliente.TipoDocumento;
import lombok.*;

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
    private LocalDate fechaNacimiento;
    private String telefono;
    private String correo;
    private String direccion;
}
