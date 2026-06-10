package com.tulicoreria.licoreria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "Kardex")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kardex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    /**
     * Stock antes del movimiento (trazabilidad)
     */
    @Column(nullable = false)
    private Integer stockAnterior;

    /**
     * Stock resultante después del movimiento
     */
    @Column(nullable = false)
    private Integer stockResultante;

    /**
     * Costo unitario en entradas por compra
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal costoUnitario;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(length = 300)
    private String motivo;

    /**
     * Referencia a la venta que originó el movimiento (si aplica)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta;

    /**
     * Referencia a la orden de compra que originó el movimiento (si aplica)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id")
    private OrdenCompra ordenCompra;

    public enum TipoMovimiento {
        ENTRADA_COMPRA,       // Llegó mercadería de una OrdenCompra
        ENTRADA_DEVOLUCION,   // Cliente devuelve un producto
        SALIDA_VENTA,         // Se vendió un producto
        SALIDA_MERMA,         // Producto dañado, vencido o roto
        AJUSTE_POSITIVO,      // Corrección manual que sube el stock
        AJUSTE_NEGATIVO       // Corrección manual que baja el stock
    }
}