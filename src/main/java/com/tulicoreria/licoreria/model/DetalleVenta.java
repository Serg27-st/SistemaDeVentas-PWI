package com.tulicoreria.licoreria.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_ventas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    /**
     * Precio unitario al momento de la venta (snapshot para historial)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /**
     * Descuento aplicado en porcentaje (0-100)
     */
    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal descuentoPorcentaje = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Calcula subtotal: cantidad * precioUnitario * (1 - descuento/100)
     */
    public void calcularSubtotal() {
        BigDecimal factor = BigDecimal.ONE.subtract(
            descuentoPorcentaje.divide(BigDecimal.valueOf(100))
        );
        this.subtotal = precioUnitario
            .multiply(BigDecimal.valueOf(cantidad))
            .multiply(factor);
    }
}
