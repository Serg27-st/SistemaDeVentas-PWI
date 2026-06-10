package com.tulicoreria.licoreria.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes_compra")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número de orden: OC-000001
     */
    @Column(nullable = false, unique = true, length = 20)
    private String numeroOrden;

    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    /**
     * Fecha esperada de llegada del pedido
     */
    private LocalDate fechaEntregaEstimada;

    /**
     * Fecha real en que se recibió la mercadería
     */
    private LocalDate fechaRecepcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrden estado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal igv;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    /**
     * Número de factura o guía que envía el proveedor
     */
    @Column(length = 50)
    private String comprobanteProveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    /**
     * Usuario que registró la orden
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleCompra> detalles = new ArrayList<>();

    @Column(length = 300)
    private String observaciones;

    public enum EstadoOrden {
        PENDIENTE,   // Orden generada, aún no llega la mercadería
        RECIBIDA,    // Mercadería recibida, stock actualizado en Kardex
        PARCIAL,     // Se recibió solo una parte del pedido
        ANULADA      // Orden cancelada
    }
}
