package com.tulicoreria.licoreria.dto;

import java.math.BigDecimal;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoRequestDTO {

    // Usado por la vista (productos/formulario.html) para decidir si es edición o creación
    // y construir la URL de submit.
    private Long id;

    private String nombre;

    private String codigo;
    private String descripcion;
    private String marca;
    private String paisOrigen;

    /** Cantidad/volumen del producto (ej: 750, 1, 355). Opcional. */
    private Double cantidad;

    /** Unidad de medida (ej: "ml", "L", "cl", "oz", "und"). Opcional. */
    private String unidadMedida;

    private BigDecimal gradoAlcohol;

    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer stock;
    private Integer stockMinimo;

    // Formato ISO (yyyy-MM-dd) para que th:field lo renderice correctamente
    // en el <input type="date"> del formulario de edición.
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaVencimiento;

    private Long categoriaId;
    private Long proveedorId;
    // Archivo subido desde el formulario
    private MultipartFile imagenFile;

    // Ruta relativa a guardar en BD (ej: "CERVEZA/mi-imagen.jpg")
    private String imagen;
}


