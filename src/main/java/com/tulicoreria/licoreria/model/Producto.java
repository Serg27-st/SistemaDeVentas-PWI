package com.tulicoreria.licoreria.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDate;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    /**
     * Código interno o de barras del producto
     */
    @Column(unique = true, length = 50)
    private String codigo;

    @Column(length = 300)
    private String descripcion;

    /**
     * 🔄 CORREGIDO: Se eliminó el campo 'rutaImagen' duplicado. Aquí se guarda
     * la ruta relativa exacta (Ej: "RON/uuid.jpg").
     */
    @Column(length = 255)
    private String imagen;

    /**
     * Marca comercial: Jack Daniel's, Johnnie Walker, Pilsen, etc.
     */
    @Column(length = 100)
    private String marca;

    /**
     * País de origen: Escocia, México, Perú, etc.
     */
    @Column(length = 80)
    private String paisOrigen;

    /**
     * Cantidad/volumen del producto (ej: 750, 1, 355).
     * Se interpreta junto con unidadMedida. Opcional para productos sin volumen definido.
     */
    @Column(nullable = true)
    private Double cantidad;

    /**
     * Unidad de medida asociada a cantidad (ej: "ml", "L", "cl", "oz", "und").
     * Opcional — null para productos sin unidad de medida.
     */
    @Column(nullable = true, length = 20)
    private String unidadMedida;

    /**
     * Grado alcohólico en porcentaje: 0.0 para bebidas sin alcohol.
     * Opcional — puede ser null para productos sin alcohol (ej: cigarros).
     */
    @Column(nullable = true, precision = 5, scale = 2)
    private BigDecimal gradoAlcohol;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    /**
     * Stock actual en unidades
     */
    @Column(nullable = false)
    private Integer stock;

    /**
     * Alerta cuando el stock baje de este umbral
     */
    @Column(nullable = false)
    private Integer stockMinimo;

    /**
 * Fecha de vencimiento del producto
 */
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Builder.Default

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    @JsonBackReference
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;
}
