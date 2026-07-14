package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.DashboardDTO;
import com.tulicoreria.licoreria.service.impl.ReporteServiceImpl.*;

import java.time.LocalDate;

public interface ReporteService {

    ReporteVentasMesDTO reporteVentasPorMes(LocalDate fechaInicio, LocalDate fechaFin);
    ReporteClientesDTO reporteVentasPorCliente(LocalDate fechaInicio, LocalDate fechaFin);
    ReporteProductosDTO reporteVentasPorProducto(LocalDate fechaInicio, LocalDate fechaFin);
    DashboardDTO dashboard();
}
