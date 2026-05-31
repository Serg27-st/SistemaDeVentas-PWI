package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromocionResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String tipo;

    private BigDecimal porcentajeDescuento;
    private BigDecimal montoDescuento;

    private Integer compraX;
    private Integer llevaY;

    private BigDecimal precioCombo;
    private String urlImagenCombo;

    private Long   productoId;
    private String productoNombre;

    private List<ComboItemDTO> items;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activo;
    private boolean vigente;
    private String etiquetaCorta;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComboItemDTO {
        private Long   productoId;
        private String productoNombre;
        private String productoMarca;
        private String urlImagen;
        private BigDecimal precioVenta;
        private int    cantidad;
    }
}
