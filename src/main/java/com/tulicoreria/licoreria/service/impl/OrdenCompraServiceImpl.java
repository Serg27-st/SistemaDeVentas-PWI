package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.exception.RecursoNoEncontradoException;
import com.tulicoreria.licoreria.exception.ReglaDeNegocioException;
import com.tulicoreria.licoreria.dto.*;
import com.tulicoreria.licoreria.model.*;
import com.tulicoreria.licoreria.model.OrdenCompra.EstadoOrden;
import com.tulicoreria.licoreria.repository.*;
import com.tulicoreria.licoreria.service.OrdenCompraService;
import com.tulicoreria.licoreria.util.GeneradorCorrelativo;
import com.tulicoreria.licoreria.util.TarifasFiscales;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenCompraServiceImpl implements OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final KardexRepository kardexRepository;

    @Override
    @Transactional
    public OrdenCompraResponseDTO crear(OrdenCompraRequestDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado"));

        List<DetalleCompra> detalles = new ArrayList<>();
        BigDecimal subtotalOrden = BigDecimal.ZERO;

        for (DetalleCompraRequestDTO detalleDTO : dto.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con id: " + detalleDTO.getProductoId()));

            BigDecimal subtotalDetalle = detalleDTO.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(detalleDTO.getCantidadSolicitada()))
                    .setScale(2, RoundingMode.HALF_UP);

            detalles.add(DetalleCompra.builder()
                    .producto(producto)
                    .cantidadSolicitada(detalleDTO.getCantidadSolicitada())
                    .cantidadRecibida(0)
                    .precioUnitario(detalleDTO.getPrecioUnitario())
                    .subtotal(subtotalDetalle)
                    .build());

            subtotalOrden = subtotalOrden.add(subtotalDetalle);
        }

        BigDecimal igvOrden   = subtotalOrden.multiply(TarifasFiscales.IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalOrden = subtotalOrden.add(igvOrden).setScale(2, RoundingMode.HALF_UP);

        OrdenCompra orden = OrdenCompra.builder()
                .numeroOrden(generarNumeroOrden())
                .fechaEmision(LocalDateTime.now())
                .fechaEntregaEstimada(dto.getFechaEntregaEstimada())
                .estado(EstadoOrden.PENDIENTE)
                .subtotal(subtotalOrden)
                .igv(igvOrden)
                .total(totalOrden)
                .proveedor(proveedor)
                .usuario(usuario)
                .observaciones(dto.getObservaciones())
                .build();

        for (DetalleCompra detalle : detalles) {
            detalle.setOrdenCompra(orden);
            orden.getDetalles().add(detalle);
        }

        return toDTO(ordenCompraRepository.save(orden));
    }

    @Override
    @Transactional
    public OrdenCompraResponseDTO recibirMercaderia(Long id, String comprobanteProveedor) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con id: " + id));

        if (orden.getEstado() == EstadoOrden.RECIBIDA) {
            throw new ReglaDeNegocioException("La orden ya fue recibida anteriormente");
        }
        if (orden.getEstado() == EstadoOrden.ANULADA) {
            throw new ReglaDeNegocioException("No se puede recibir una orden anulada");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        for (DetalleCompra detalle : orden.getDetalles()) {
            Producto producto = detalle.getProducto();
            int stockAnterior = producto.getStock();
            detalle.setCantidadRecibida(detalle.getCantidadSolicitada());
            producto.setStock(producto.getStock() + detalle.getCantidadSolicitada());
            productoRepository.save(producto);

            kardexRepository.save(Kardex.builder()
                    .tipo(Kardex.TipoMovimiento.ENTRADA_COMPRA)
                    .producto(producto)
                    .cantidad(detalle.getCantidadSolicitada())
                    .stockAnterior(stockAnterior)
                    .stockResultante(producto.getStock())
                    .costoUnitario(detalle.getPrecioUnitario())
                    .fechaHora(LocalDateTime.now())
                    .usuario(usuario)
                    .motivo("Recepción orden: " + orden.getNumeroOrden())
                    .ordenCompra(orden)
                    .build());
        }

        orden.setEstado(EstadoOrden.RECIBIDA);
        orden.setFechaRecepcion(LocalDate.now());
        orden.setComprobanteProveedor(comprobanteProveedor);
        return toDTO(ordenCompraRepository.save(orden));
    }

    @Override
    @Transactional
    public OrdenCompraResponseDTO anular(Long id) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con id: " + id));
        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new ReglaDeNegocioException(
                "Solo se pueden anular órdenes PENDIENTES. Estado actual: " + orden.getEstado());
        }
        orden.setEstado(EstadoOrden.ANULADA);
        return toDTO(ordenCompraRepository.save(orden));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraResponseDTO> listarTodas() {
        return ordenCompraRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraResponseDTO> listarPendientes() {
        return ordenCompraRepository.findByEstado(EstadoOrden.PENDIENTE)
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenCompraResponseDTO buscarPorId(Long id) {
        return toDTO(ordenCompraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con id: " + id)));
    }

    private String generarNumeroOrden() {
        return GeneradorCorrelativo.siguiente("OC", ordenCompraRepository.findUltimoNumeroOrden());
    }

    private OrdenCompraResponseDTO toDTO(OrdenCompra o) {
        List<DetalleCompraResponseDTO> detallesDTO = o.getDetalles().stream()
                .map(d -> DetalleCompraResponseDTO.builder()
                        .id(d.getId())
                        .productoNombre(d.getProducto().getNombre())
                        .productoCodigo(d.getProducto().getCodigo())
                        .productoMarca(d.getProducto().getMarca())
                        .productoCantidad(d.getProducto().getCantidad())
                        .productoUnidad(d.getProducto().getUnidadMedida())
                        .cantidadSolicitada(d.getCantidadSolicitada())
                        .cantidadRecibida(d.getCantidadRecibida())
                        .cantidadPendiente(d.getCantidadSolicitada() - d.getCantidadRecibida())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .toList();

        return OrdenCompraResponseDTO.builder()
                .id(o.getId())
                .numeroOrden(o.getNumeroOrden())
                .fechaEmision(o.getFechaEmision())
                .fechaEntregaEstimada(o.getFechaEntregaEstimada())
                .fechaRecepcion(o.getFechaRecepcion())
                .estado(o.getEstado().name())
                .comprobanteProveedor(o.getComprobanteProveedor())
                .proveedorRazonSocial(o.getProveedor().getRazonSocial())
                .proveedorRuc(o.getProveedor().getRuc())
                .usuarioNombre(o.getUsuario().getNombreCompleto())
                .subtotal(o.getSubtotal())
                .igv(o.getIgv())
                .total(o.getTotal())
                .observaciones(o.getObservaciones())
                .detalles(detallesDTO)
                .build();
    }
}
