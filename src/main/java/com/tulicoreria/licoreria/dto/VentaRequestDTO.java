package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaRequestDTO {

    // Cliente opcional — null significa venta a cliente anónimo
    private Long clienteId;

    private String tipoComprobante;  // "BOLETA", "FACTURA", "TICKET"
    private String metodoPago;       // "EFECTIVO", "YAPE", "PLIN", "TARJETA"
    private String observaciones;

    // Al menos un detalle es obligatorio
    private List<DetalleVentaRequestDTO> detalles;
}
 