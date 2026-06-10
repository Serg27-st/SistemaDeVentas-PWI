package com.tulicoreria.licoreria.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromocionRequestDTO {

    private String nombre;
    private String descripcion;
    private String tipo;               // DESCUENTO_DIRECTO | VOLUMEN | COMBO

    // DESCUENTO_DIRECTO
    private BigDecimal porcentajeDescuento;
    private BigDecimal montoDescuento;

    // VOLUMEN
    private Integer compraX;
    private Integer llevaY;

    // COMBO
    private BigDecimal precioCombo;
    private MultipartFile imagenComboFile;

    // Producto vinculado (DESCUENTO_DIRECTO / VOLUMEN)
    private Long productoId;

    // Ítems del combo (listas paralelas)
    private List<Long>    comboProductoIds;
    private List<Integer> comboCantidades;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activo = true;
}
