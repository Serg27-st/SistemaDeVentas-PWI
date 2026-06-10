package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.util.Set;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequestDTO {

    private String username;
    private String password;
    private String nombreCompleto;
    private String correo;
    private Set<Long> rolIds;
}
