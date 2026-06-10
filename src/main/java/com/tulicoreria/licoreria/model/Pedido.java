package com.tulicoreria.licoreria.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido web — representa una orden creada desde el carrito público.
 * Flujo: PENDIENTE_PAGO → PAGADO (online) | POR_ENTREGAR (contra entrega) → ENTREGADO
 *                       ↘ CANCELADO (fallo de pago o expiración)
 */
@Entity
@Table(name = "pedidos")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numeroPedido;           // PED-000001

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    // ── Totales ────────────────────────────────────────────────────────────
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal igv;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    // ── Datos del cliente (denormalizados para historial) ──────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_web_id")
    private ClienteWeb clienteWeb;         // null para invitados

    @Column(length = 80)
    private String nombreCliente;

    @Column(length = 80)
    private String apellidoCliente;

    @Column(length = 150)
    private String emailCliente;

    @Column(length = 20)
    private String telefonoCliente;

    @Column(length = 15)
    private String dniCliente;

    // ── Comprobante ────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TipoComprobante tipoComprobante;

    @Column(length = 15)
    private String rucCliente;

    @Column(length = 120)
    private String razonSocial;

    // ── Dirección de entrega ───────────────────────────────────────────────
    @Column(length = 200)
    private String direccion;

    @Column(length = 60)
    private String departamento;

    @Column(length = 200)
    private String referencia;

    @Column(length = 80)
    private String distrito;

    // ── Pago ───────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MetodoPago metodoPago;

    @Column(length = 60)
    private String culqiChargeId;

    @Column(length = 200)
    private String errorPago;              // mensaje si el cobro falló

    // ── Timestamps ─────────────────────────────────────────────────────────
    @Column(nullable = false)
    private LocalDateTime creadoEn;

    private LocalDateTime expiraEn;        // reserva de stock (15 min)
    private LocalDateTime pagadoEn;

    // ── Ítems ──────────────────────────────────────────────────────────────
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PedidoItem> items = new ArrayList<>();

    // ── Enums ──────────────────────────────────────────────────────────────
    public enum EstadoPedido {
        PENDIENTE_PAGO,   // creado, esperando pago online
        PAGADO,           // pago online confirmado
        POR_ENTREGAR,     // contra entrega — pago pendiente en puerta
        CANCELADO,        // cancelado (fallo de pago o expiración)
        ENTREGADO         // entregado al cliente
    }

    public enum TipoComprobante {
        BOLETA, FACTURA
    }

    public enum MetodoPago {
        TARJETA, CONTRA_ENTREGA, YAPE, PLIN
    }
}
