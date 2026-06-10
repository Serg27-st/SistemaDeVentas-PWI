package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompraRequestDTO {

    private Long proveedorId;
    private LocalDate fechaEntregaEstimada;
    private String observaciones;
    private List<DetalleCompraRequestDTO> detalles;
}
