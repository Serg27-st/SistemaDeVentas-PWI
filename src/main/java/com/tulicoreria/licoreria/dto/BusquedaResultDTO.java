package com.tulicoreria.licoreria.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO ligero para resultados de búsqueda (autocompletado y API pública).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusquedaResultDTO {

    private Long id;
    private String nombre;
    private String marca;
    private String categoriaNombre;
    private BigDecimal cantidadPresentacion;
    private String unidadPresentacion;
    private BigDecimal precioVenta;
    private String urlImagen;
    private boolean sinStock;
}
