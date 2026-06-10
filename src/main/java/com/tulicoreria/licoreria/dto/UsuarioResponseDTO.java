package com.tulicoreria.licoreria.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
