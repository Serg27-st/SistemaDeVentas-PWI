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

    // Compatibilidad: evitar dependencia total de Lombok para el getter del productoId
    // (resuelve errores "getProductoId" si Lombok no está generando getters correctamente).
    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }
}

