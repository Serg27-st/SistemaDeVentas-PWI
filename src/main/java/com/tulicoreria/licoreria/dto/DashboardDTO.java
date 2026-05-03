package com.tulicoreria.licoreria.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    // Resumen del día
    private BigDecimal totalVentasHoy;
    private Long cantidadVentasHoy;

    // Resumen del mes
    private BigDecimal totalVentasMes;
    private Long cantidadVentasMes;

    // Alertas de inventario
    private int productosConStockBajo;
    private int productosSinStock;

    // Órdenes de compra pendientes de recibir
    private int ordenesPendientes;

    // Los 5 productos más vendidos del mes
    private List<String> productosMasVendidos;
}
