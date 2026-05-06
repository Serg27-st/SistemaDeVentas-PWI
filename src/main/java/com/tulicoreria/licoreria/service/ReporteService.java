package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.DashboardDTO;
import com.tulicoreria.licoreria.service.impl.ReporteServiceImpl.*;

public interface ReporteService {

    ReporteVentasMesDTO reporteVentasPorMes(int mes, int anio);
    ReporteClientesDTO reporteVentasPorCliente(int mes, int anio);
    ReporteProductosDTO reporteVentasPorProducto(int mes, int anio);
    DashboardDTO dashboard();
}
