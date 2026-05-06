package com.tulicoreria.licoreria.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
