package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleVentaRequestDTO {

    private Long productoId;
    private Integer cantidad;

    // Descuento opcional por línea, por defecto 0
    @Builder.Default
    private BigDecimal descuentoPorcentaje = BigDecimal.ZERO;
}
