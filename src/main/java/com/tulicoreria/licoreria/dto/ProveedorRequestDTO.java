package com.tulicoreria.licoreria.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorRequestDTO {

    private String razonSocial;
    private String ruc;
    private String direccion;
    private String telefono;
    private String correo;
    private String contactoNombre;
}
