package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCompraRequestDTO {

    private Long productoId;
    private Integer cantidadSolicitada;
    private BigDecimal precioUnitario;
}
