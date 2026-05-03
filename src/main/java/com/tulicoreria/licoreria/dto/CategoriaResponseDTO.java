package com.tulicoreria.licoreria.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private boolean activo;

    // Cantidad de productos en esta categoría (para mostrar en la lista)
    private int totalProductos;
}
