package com.tulicoreria.licoreria.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorResponseDTO {

    private Long id;
    private String razonSocial;
    private String ruc;
    private String direccion;
    private String telefono;
    private String correo;
    private String contactoNombre;
    private boolean activo;

    // Cantidad de órdenes de compra realizadas a este proveedor
    private int totalOrdenes;
}
