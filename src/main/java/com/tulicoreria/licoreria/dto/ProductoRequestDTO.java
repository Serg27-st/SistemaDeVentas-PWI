package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoRequestDTO {

    private String nombre;
    private String codigo;
    private String descripcion;
    private String marca;
    private String paisOrigen;
    private Integer volumenMl;
    private BigDecimal gradoAlcohol;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer stockMinimo;

    // Solo IDs — el Service busca los objetos completos con el Repository
    private Long categoriaId;
    private Long proveedorId;
}
