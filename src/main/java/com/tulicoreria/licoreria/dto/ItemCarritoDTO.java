package com.tulicoreria.licoreria.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCarritoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productoId;
    private String nombre;
    private String marca;
    private String urlImagen;
    private BigDecimal precioUnitario;
    private int cantidad;

    /** Descuento monetario aplicado (volumen, combo, etc.). Null = sin descuento. */
    private BigDecimal descuentoAplicado;
    /** Etiqueta corta de la promo: "2×1 — 1 gratis", "Pack Chilcanero", etc. */
    private String etiquetaPromo;

    public BigDecimal getSubtotal() {
        BigDecimal bruto = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        if (descuentoAplicado != null && descuentoAplicado.compareTo(BigDecimal.ZERO) > 0) {
            return bruto.subtract(descuentoAplicado).max(BigDecimal.ZERO);
        }
        return bruto;
    }
}
