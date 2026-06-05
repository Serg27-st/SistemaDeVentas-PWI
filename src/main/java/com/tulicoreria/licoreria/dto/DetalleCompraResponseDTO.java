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
public class DetalleCompraResponseDTO {

    private Long id;
    private String productoNombre;
    private String productoCodigo;
    private String productoMarca;
    private BigDecimal cantidadPresentacion;
    private String unidadPresentacion;
    private Integer cantidadSolicitada;
    private Integer cantidadRecibida;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    // Calculado: cantidadSolicitada - cantidadRecibida
    // Útil para mostrar cuánto falta recibir en entregas parciales
    private Integer cantidadPendiente;
}
