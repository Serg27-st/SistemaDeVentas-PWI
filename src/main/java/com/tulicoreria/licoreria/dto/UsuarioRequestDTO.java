package com.tulicoreria.licoreria.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequestDTO {

    private String username;
    private String password;
    private String nombreCompleto;
    private String correo;

    // Un usuario interno tiene exactamente un rol (Administrador, Vendedor o Almacén)
    private Long rolId;
}
