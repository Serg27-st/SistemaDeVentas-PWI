package com.tulicoreria.licoreria.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaResponseDTO {

    private Long id;
    private String numeroComprobante;
    private String tipoComprobante;
    private LocalDateTime fechaHora;
    private String estado;
    private String metodoPago;

    // Datos del cliente — puede ser "Cliente anónimo" si no se registró
    private String clienteNombre;
    private String clienteDocumento;

    // Nombre del vendedor que procesó la venta
    private String vendedorNombre;

    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal costoEnvio;
    private BigDecimal total;

    private String observaciones;
    private String distritoEnvio;

    private List<DetalleVentaResponseDTO> detalles;
}
