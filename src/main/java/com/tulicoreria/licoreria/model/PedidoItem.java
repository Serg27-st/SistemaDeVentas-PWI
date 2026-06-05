package com.tulicoreria.licoreria.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pedido_items")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    /** ID del producto en la BD (puede ser null para ítems de combo virtuales). */
    private Long productoId;

    @Column(length = 120)
    private String productoNombre;

    @Column(length = 80)
    private String productoMarca;

    /** Etiqueta de promoción aplicada, e.g. "2×1", "Pack Chilcanero". */
    @Column(length = 80)
    private String etiquetaPromo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private int cantidad;

    /** Descuento monetario aplicado al ítem (puede ser 0). */
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}
