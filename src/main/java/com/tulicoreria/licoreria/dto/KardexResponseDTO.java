package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KardexResponseDTO {

    private Long id;
    private String tipo;               // "ENTRADA_COMPRA", "SALIDA_VENTA", etc.
    private String productoNombre;
    private String productoCodigo;
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockResultante;
    private BigDecimal costoUnitario;
    private LocalDateTime fechaHora;
    private String usuarioNombre;
    private String motivo;

    // Referencia al documento origen — puede ser número de venta u orden de compra
    private String documentoOrigen;    // "BOLETA-000001" o "OC-000001"
}
