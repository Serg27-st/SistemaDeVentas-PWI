package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.dto.KardexResponseDTO;
import com.tulicoreria.licoreria.model.Kardex;
import com.tulicoreria.licoreria.model.Producto;
import com.tulicoreria.licoreria.repository.KardexRepository;
import com.tulicoreria.licoreria.repository.ProductoRepository;
import com.tulicoreria.licoreria.service.KardexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KardexServiceImpl implements KardexService {

    private final KardexRepository kardexRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<KardexResponseDTO> historialPorProducto(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + productoId));
        return kardexRepository.findByProductoOrderByFechaHoraDesc(producto)
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KardexResponseDTO> ultimosMovimientos(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + productoId));
        return kardexRepository.findTop10ByProductoOrderByFechaHoraDesc(producto)
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KardexResponseDTO> movimientosPorRango(LocalDateTime inicio, LocalDateTime fin) {
        return kardexRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(inicio, fin)
                .stream().map(this::toDTO).toList();
    }

    private KardexResponseDTO toDTO(Kardex k) {
        String documentoOrigen = null;
        if (k.getVenta() != null) {
            documentoOrigen = k.getVenta().getNumeroComprobante();
        } else if (k.getOrdenCompra() != null) {
            documentoOrigen = k.getOrdenCompra().getNumeroOrden();
        }
        return KardexResponseDTO.builder()
                .id(k.getId())
                .tipo(k.getTipo().name())
                .productoNombre(k.getProducto().getNombre())
                .productoCodigo(k.getProducto().getCodigo())
                .cantidad(k.getCantidad())
                .stockAnterior(k.getStockAnterior())
                .stockResultante(k.getStockResultante())
                .costoUnitario(k.getCostoUnitario())
                .fechaHora(k.getFechaHora())
                .usuarioNombre(k.getUsuario().getNombreCompleto())
                .motivo(k.getMotivo())
                .documentoOrigen(documentoOrigen)
                .build();
    }
}
