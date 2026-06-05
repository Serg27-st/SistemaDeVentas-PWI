package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroClienteWebDTO {

    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String confirmarPassword;
    private String telefono;
    private LocalDate fechaNacimiento;
}
