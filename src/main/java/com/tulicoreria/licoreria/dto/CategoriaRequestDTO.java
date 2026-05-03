package com.tulicoreria.licoreria.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaRequestDTO {

    private String nombre;
    private String descripcion;
}
