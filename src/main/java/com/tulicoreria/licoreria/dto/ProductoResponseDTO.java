package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponseDTO {

    private Long id;
    private String nombre;
    private String codigo;
    private String marca;
    private String paisOrigen;
    private Integer volumenMl;
    private BigDecimal gradoAlcohol;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer stock;
    private Integer stockMinimo;
    private boolean activo;

    // Nombres en vez de objetos completos — más limpio para la vista
    private String categoriaNombre;
    private String proveedorRazonSocial;

    // Calculado en el Service: stock <= stockMinimo
    private boolean stockBajo;

    // Calculado en el Service: stock == 0
    private boolean sinStock;
}
