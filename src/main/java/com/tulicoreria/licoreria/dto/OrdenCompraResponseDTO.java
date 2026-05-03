package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompraResponseDTO {

    private Long id;
    private String numeroOrden;
    private LocalDateTime fechaEmision;
    private LocalDate fechaEntregaEstimada;
    private LocalDate fechaRecepcion;
    private String estado;
    private String comprobanteProveedor;

    // Datos del proveedor
    private String proveedorRazonSocial;
    private String proveedorRuc;

    // Usuario que generó la orden
    private String usuarioNombre;

    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;

    private String observaciones;

    private List<DetalleCompraResponseDTO> detalles;
}
