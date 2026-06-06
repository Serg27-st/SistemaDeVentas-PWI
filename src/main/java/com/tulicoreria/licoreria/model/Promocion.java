package com.tulicoreria.licoreria.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promociones")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TipoPromocion tipo;

    // ── DESCUENTO_DIRECTO ────────────────────────────────────────────────────
    /** Descuento como porcentaje (ej. 15.00 = 15 %). Mutuamente excluyente con montoDescuento. */
    @Column(precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;

    /** Descuento como monto fijo en soles (ej. 20.00 = S/. 20 off). */
    @Column(precision = 10, scale = 2)
    private BigDecimal montoDescuento;

    // ── VOLUMEN (2×1, 3×2, etc.) ─────────────────────────────────────────────
    /** Cantidad que el cliente debe comprar. Ej. 2 para una promo 2×1. */
    private Integer compraX;

    /** Unidades que el cliente paga. Ej. 1 para una promo 2×1 (paga 1, lleva 2). */
    private Integer llevaY;

    // ── COMBO ────────────────────────────────────────────────────────────────
    /** Precio total del combo (menor que la suma de los productos). */
    @Column(precision = 10, scale = 2)
    private BigDecimal precioCombo;

    /** Ruta de imagen representativa del pack. */
    @Column(length = 255)
    private String imagenCombo;

    // ── Producto vinculado (DESCUENTO_DIRECTO y VOLUMEN) ─────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    // ── Ítems del combo (COMBO) ───────────────────────────────────────────────
    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ComboItem> items = new ArrayList<>();

    // ── Vigencia ─────────────────────────────────────────────────────────────
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    // ── Helpers ───────────────────────────────────────────────────────────────

    public boolean esActiva() {
        if (!activo) return false;
        LocalDate hoy = LocalDate.now();
        if (fechaInicio != null && hoy.isBefore(fechaInicio)) return false;
        if (fechaFin != null && hoy.isAfter(fechaFin)) return false;
        return true;
    }

    /** Etiqueta corta para mostrar en tarjetas: "−15 %", "Lleva 2 paga 1", "Pack" */
    public String etiquetaCorta() {
        return switch (tipo) {
            case DESCUENTO_DIRECTO -> porcentajeDescuento != null
                    ? "−" + porcentajeDescuento.stripTrailingZeros().toPlainString() + " %"
                    : (montoDescuento != null ? "−S/. " + montoDescuento.toPlainString() : "Descuento");
            case VOLUMEN -> (compraX != null && llevaY != null)
                    ? "Lleva " + compraX + " paga " + llevaY
                    : "Volumen";
            case COMBO   -> "Pack";
        };
    }

    public enum TipoPromocion {
        DESCUENTO_DIRECTO, VOLUMEN, COMBO
    }
}
