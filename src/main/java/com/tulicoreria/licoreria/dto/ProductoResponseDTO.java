package com.tulicoreria.licoreria.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;


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
    private BigDecimal cantidadPresentacion;
    private String unidadPresentacion;
    private BigDecimal gradoAlcohol;
    private BigDecimal precision;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer stock;
    private Integer stockMinimo;
    
    private LocalDate fechaVencimiento;

    private boolean activo;

    private Long categoriaId;
    private Long proveedorId;

    private String categoriaNombre;
    private String proveedorRazonSocial;

    private boolean stockBajo;
    private boolean sinStock;

    private String urlImagen;

    // ── Campos de promoción ───────────────────────────────────────────────────
    private boolean tienePromocion;
    /** Precio original tachado (solo si hay DESCUENTO_DIRECTO activo). */
    private BigDecimal precioOriginal;
    /** Etiqueta corta: "−15 %", "2×1", "COMBO". */
    private String etiquetaPromo;
    /** ID de la promoción vinculada (para operaciones en carrito). */
    private Long promocionId;
    /** Tipo de promo: DESCUENTO_DIRECTO | VOLUMEN | COMBO */
    private String tipoPromocion;
}
