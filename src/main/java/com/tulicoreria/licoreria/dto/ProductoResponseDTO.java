package com.tulicoreria.licoreria.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponseDTO {

    private Long id;
    private String nombre;
    private String codigo;
    private String descripcion; // 👈 ¡AÑADE ESTA LÍNEA AQUÍ!
    private String marca;
    private String paisOrigen;
    private Integer volumenMl;
    private BigDecimal gradoAlcohol;
    private BigDecimal precision;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer stock;
    private Integer stockMinimo;
    private boolean activo;

    private Long categoriaId;
    private Long proveedorId;

    private String categoriaNombre;
    private String proveedorRazonSocial;

    private boolean stockBajo;
    private boolean sinStock;

    private String urlImagen;
}
