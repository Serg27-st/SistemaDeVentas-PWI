package com.tulicoreria.licoreria.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tulicoreria.licoreria.dto.DetalleVentaRequestDTO;
import com.tulicoreria.licoreria.dto.DetalleVentaResponseDTO;
import com.tulicoreria.licoreria.dto.ItemCarritoDTO;
import com.tulicoreria.licoreria.dto.VentaRequestDTO;
import com.tulicoreria.licoreria.dto.VentaResponseDTO;
import com.tulicoreria.licoreria.model.Cliente;
import com.tulicoreria.licoreria.model.DetalleVenta;
import com.tulicoreria.licoreria.model.Kardex;
import com.tulicoreria.licoreria.model.Producto;
import com.tulicoreria.licoreria.model.Usuario;
import com.tulicoreria.licoreria.model.Venta;
import com.tulicoreria.licoreria.model.Venta.EstadoVenta;
import com.tulicoreria.licoreria.model.Venta.MetodoPago;
import com.tulicoreria.licoreria.model.Venta.TipoComprobante;
import com.tulicoreria.licoreria.repository.ClienteRepository;
import com.tulicoreria.licoreria.repository.KardexRepository;
import com.tulicoreria.licoreria.repository.ProductoRepository;
import com.tulicoreria.licoreria.repository.UsuarioRepository;
import com.tulicoreria.licoreria.repository.VentaRepository;
import com.tulicoreria.licoreria.service.VentaService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
@Data
@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final KardexRepository kardexRepository;

    private static final BigDecimal IGV = new BigDecimal("0.18");

    @Override
    @Transactional
    public VentaResponseDTO registrar(VentaRequestDTO dto) {

        // 1. Vendedor actual desde Spring Security
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario vendedor = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        // 2. Cliente opcional
        Cliente cliente = null;
        if (dto.getClienteId() != null) {
            cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            // 3. Verificar mayoría de edad
            if (!cliente.esMayorDeEdad()) {
                throw new RuntimeException(
                    "El cliente " + cliente.getNombre() + " es menor de edad. " +
                    "No se puede realizar la venta de alcohol."
                );
            }
        }

        // 4. Construir detalles y validar stock
        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal subtotalVenta = BigDecimal.ZERO;

        for (DetalleVentaRequestDTO detalleDTO : dto.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException(
                        "Producto no encontrado con id: " + detalleDTO.getProductoId()));

            // 5. Validar stock disponible
            if (producto.getStock() < detalleDTO.getCantidad()) {
                throw new RuntimeException(
                    "Stock insuficiente para: " + producto.getNombre() +
                    ". Disponible: " + producto.getStock() +
                    ", solicitado: " + detalleDTO.getCantidad()
                );
            }

            // 6. Calcular subtotal del detalle con descuento
            BigDecimal descuento = detalleDTO.getDescuentoPorcentaje() != null
                    ? detalleDTO.getDescuentoPorcentaje() : BigDecimal.ZERO;

            BigDecimal factor = BigDecimal.ONE.subtract(
                    descuento.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));

            BigDecimal subtotalDetalle = producto.getPrecioVenta()
                    .multiply(BigDecimal.valueOf(detalleDTO.getCantidad()))
                    .multiply(factor)
                    .setScale(2, RoundingMode.HALF_UP);

            DetalleVenta detalle = DetalleVenta.builder()
                    .producto(producto)
                    .cantidad(detalleDTO.getCantidad())
                    .precioUnitario(producto.getPrecioVenta())
                    .descuentoPorcentaje(descuento)
                    .subtotal(subtotalDetalle)
                    .build();

            detalles.add(detalle);
            subtotalVenta = subtotalVenta.add(subtotalDetalle);

            // 7. Descontar stock
            producto.setStock(producto.getStock() - detalleDTO.getCantidad());
            productoRepository.save(producto);
        }

        // 8. Calcular IGV y total
        BigDecimal igvVenta = subtotalVenta.multiply(IGV).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalVenta = subtotalVenta.add(igvVenta).setScale(2, RoundingMode.HALF_UP);

        // 9. Generar número de comprobante
        TipoComprobante tipoComprobante = TipoComprobante.valueOf(dto.getTipoComprobante());
        String numeroComprobante = generarNumeroComprobante(tipoComprobante);

        // 10. Crear venta
        Venta venta = Venta.builder()
                .numeroComprobante(numeroComprobante)
                .tipoComprobante(tipoComprobante)
                .fechaHora(LocalDateTime.now())
                .subtotal(subtotalVenta)
                .igv(igvVenta)
                .total(totalVenta)
                .estado(EstadoVenta.COMPLETADA)
                .metodoPago(MetodoPago.valueOf(dto.getMetodoPago()))
                .cliente(cliente)
                .vendedor(vendedor)
                .observaciones(dto.getObservaciones())
                .build();

        // 11. Asociar detalles — cascade los guarda automáticamente
        for (DetalleVenta detalle : detalles) {
            detalle.setVenta(venta);
            venta.getDetalles().add(detalle);
        }

        Venta ventaGuardada = ventaRepository.save(venta);

        // 12. Registrar en Kardex
        for (DetalleVenta detalle : detalles) {
            Producto producto = detalle.getProducto();
            kardexRepository.save(Kardex.builder()
                    .tipo(Kardex.TipoMovimiento.SALIDA_VENTA)
                    .producto(producto)
                    .cantidad(detalle.getCantidad())
                    .stockAnterior(producto.getStock() + detalle.getCantidad())
                    .stockResultante(producto.getStock())
                    .fechaHora(LocalDateTime.now())
                    .usuario(vendedor)
                    .motivo("Venta: " + numeroComprobante)
                    .venta(ventaGuardada)
                    .build());
        }

        return toDTO(ventaGuardada);
    }

    @Override
    @Transactional
    public VentaResponseDTO registrarDesdeCarrito(List<ItemCarritoDTO> items, String metodoPago) {

        if (items == null || items.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // Vendedor sistema que representa el canal web
        Usuario vendedor = usuarioRepository.findByUsername("tienda_online")
                .orElseThrow(() -> new RuntimeException(
                    "Usuario sistema 'tienda_online' no encontrado. " +
                    "Reinicia la aplicación para que sea creado automáticamente."));

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal subtotalVenta = BigDecimal.ZERO;

        for (ItemCarritoDTO item : items) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException(
                        "Producto no encontrado: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException(
                    "Stock insuficiente para \"" + producto.getNombre() + "\". " +
                    "Disponible: " + producto.getStock() +
                    ", solicitado: " + item.getCantidad());
            }

            BigDecimal subtotalDetalle = producto.getPrecioVenta()
                    .multiply(BigDecimal.valueOf(item.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP);

            detalles.add(DetalleVenta.builder()
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(producto.getPrecioVenta())
                    .descuentoPorcentaje(BigDecimal.ZERO)
                    .subtotal(subtotalDetalle)
                    .build());

            subtotalVenta = subtotalVenta.add(subtotalDetalle);

            // Descontar stock inmediatamente
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
        }

        BigDecimal igvVenta    = subtotalVenta.multiply(IGV).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalVenta  = subtotalVenta.add(igvVenta).setScale(2, RoundingMode.HALF_UP);
        String     comprobante = generarNumeroComprobante(TipoComprobante.TICKET);

        MetodoPago mp;
        try {
            mp = MetodoPago.valueOf(metodoPago.toUpperCase());
        } catch (Exception e) {
            mp = MetodoPago.EFECTIVO;
        }

        Venta venta = Venta.builder()
                .numeroComprobante(comprobante)
                .tipoComprobante(TipoComprobante.TICKET)
                .fechaHora(LocalDateTime.now())
                .subtotal(subtotalVenta)
                .igv(igvVenta)
                .total(totalVenta)
                .estado(EstadoVenta.PENDIENTE)
                .metodoPago(mp)
                .cliente(null)
                .vendedor(vendedor)
                .observaciones("Pedido web — Tienda online")
                .build();

        for (DetalleVenta d : detalles) {
            d.setVenta(venta);
            venta.getDetalles().add(d);
        }

        Venta ventaGuardada = ventaRepository.save(venta);

        // Registrar salidas en Kardex
        for (DetalleVenta d : detalles) {
            Producto p = d.getProducto();
            kardexRepository.save(Kardex.builder()
                    .tipo(Kardex.TipoMovimiento.SALIDA_VENTA)
                    .producto(p)
                    .cantidad(d.getCantidad())
                    .stockAnterior(p.getStock() + d.getCantidad())
                    .stockResultante(p.getStock())
                    .fechaHora(LocalDateTime.now())
                    .usuario(vendedor)
                    .motivo("Venta web: " + comprobante)
                    .venta(ventaGuardada)
                    .build());
        }

        return toDTO(ventaGuardada);
    }

    @Override
    @Transactional
    public VentaResponseDTO completar(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con id: " + id));

        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new RuntimeException("Solo se pueden completar ventas en estado PENDIENTE");
        }

        venta.setEstado(EstadoVenta.COMPLETADA);
        return toDTO(ventaRepository.save(venta));
    }

    @Override
    @Transactional
    public VentaResponseDTO anular(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con id: " + id));

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new RuntimeException("La venta ya se encuentra anulada");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Devolver stock y registrar en Kardex
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            int stockAnterior = producto.getStock();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);

            kardexRepository.save(Kardex.builder()
                    .tipo(Kardex.TipoMovimiento.ENTRADA_DEVOLUCION)
                    .producto(producto)
                    .cantidad(detalle.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockResultante(producto.getStock())
                    .fechaHora(LocalDateTime.now())
                    .usuario(usuario)
                    .motivo("Anulación venta: " + venta.getNumeroComprobante())
                    .venta(venta)
                    .build());
        }

        venta.setEstado(EstadoVenta.ANULADA);
        return toDTO(ventaRepository.save(venta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarTodas() {
        return ventaRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO buscarPorId(Long id) {
        return toDTO(ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con id: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO buscarPorComprobante(String numeroComprobante) {
        return toDTO(ventaRepository.findByNumeroComprobante(numeroComprobante)
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado: " + numeroComprobante)));
    }

    private String generarNumeroComprobante(TipoComprobante tipo) {
        String ultimo = ventaRepository.findUltimoNumeroComprobante(tipo);
        int siguiente = 1;
        if (ultimo != null) {
            siguiente = Integer.parseInt(ultimo.split("-")[1]) + 1;
        }
        return tipo.name() + "-" + String.format("%06d", siguiente);
    }

    @Override
    public VentaResponseDTO toDTO(Venta v) {
        List<DetalleVentaResponseDTO> detallesDTO = v.getDetalles().stream() 
                .map(d-> DetalleVentaResponseDTO.builder()
                        .id(d.getId().longValue())
                        .productoNombre(d.getProducto().getNombre())
                        .productoCodigo(d.getProducto().getCodigo())
                        .productoMarca(d.getProducto().getMarca())
                        .volumenMl(d.getProducto().getVolumenMl())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .descuentoPorcentaje(d.getDescuentoPorcentaje())
                        .subtotal(d.getSubtotal())
                        .build())
                .toList();


        return VentaResponseDTO.builder()
                .id(v.getId())
                .numeroComprobante(v.getNumeroComprobante())
                .tipoComprobante(v.getTipoComprobante().name())
                .fechaHora(v.getFechaHora())
                .estado(v.getEstado().name())
                .metodoPago(v.getMetodoPago().name())
                .clienteNombre(v.getCliente() != null
                        ? v.getCliente().getNombre() + " " + v.getCliente().getApellido()
                        : "Cliente anónimo")
                .clienteDocumento(v.getCliente() != null
                        ? v.getCliente().getNumeroDocumento() : "-")
                .vendedorNombre(v.getVendedor().getNombreCompleto())
                .subtotal(v.getSubtotal())
                .igv(v.getIgv())
                .total(v.getTotal())
                .observaciones(v.getObservaciones())
                .detalles(detallesDTO)
                .build();
    }
}
