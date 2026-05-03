package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.util.Set;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Long id;
    private String username;
    private String nombreCompleto;
    private String correo;
    private boolean activo;

    // Nombres de los roles: "ROLE_ADMIN", "ROLE_VENDEDOR", etc.
    private Set<String> roles;
}
