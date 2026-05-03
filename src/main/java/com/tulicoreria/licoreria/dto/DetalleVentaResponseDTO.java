package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleVentaResponseDTO {

    private Long id;
    private String productoNombre;
    private String productoCodigo;
    private String productoMarca;
    private Integer volumenMl;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal subtotal;
}
