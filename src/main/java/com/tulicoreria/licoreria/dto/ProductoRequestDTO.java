package com.tulicoreria.licoreria.dto;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

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
public class ProductoRequestDTO {

    // Usado por la vista (productos/formulario.html) para decidir si es edición o creación
    // y construir la URL de submit.
    private Long id;

    private String nombre;

    private String codigo;
    private String descripcion;
    private String marca;
    private String paisOrigen;

    // 💡 Mantenemos este nombre para tu HTML, pero asegúrate de mapearlo bien en el Service
    private Integer volumenMl;

    // 🔑 CORREGIDO: Cambiado de BigDecimal a Double para evitar conflictos de tipo con la Entidad
    private BigDecimal gradoAlcohol;

    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer stock;
    private Integer stockMinimo;

    private Long categoriaId;
    private Long proveedorId;
    // Archivo subido desde el formulario
    private MultipartFile imagenFile;

    // Ruta relativa a guardar en BD (ej: "CERVEZA/mi-imagen.jpg")
    private String imagen;
}


