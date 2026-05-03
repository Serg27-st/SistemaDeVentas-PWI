package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCompraResponseDTO {

    private Long id;
    private String productoNombre;
    private String productoCodigo;
    private String productoMarca;
    private Integer volumenMl;
    private Integer cantidadSolicitada;
    private Integer cantidadRecibida;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    // Calculado: cantidadSolicitada - cantidadRecibida
    // Útil para mostrar cuánto falta recibir en entregas parciales
    private Integer cantidadPendiente;
}
