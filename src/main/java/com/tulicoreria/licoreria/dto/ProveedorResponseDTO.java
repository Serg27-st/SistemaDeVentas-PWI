package com.tulicoreria.licoreria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
