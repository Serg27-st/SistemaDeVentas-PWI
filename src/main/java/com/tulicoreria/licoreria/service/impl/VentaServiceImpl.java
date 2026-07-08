package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.exception.RecursoNoEncontradoException;
import com.tulicoreria.licoreria.exception.ReglaDeNegocioException;
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
import com.tulicoreria.licoreria.model.Pedido;
import com.tulicoreria.licoreria.model.PedidoItem;
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
import com.tulicoreria.licoreria.util.GeneradorCorrelativo;
import com.tulicoreria.licoreria.util.TarifasFiscales;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final KardexRepository kardexRepository;

    @Override
    @Transactional
    public VentaResponseDTO registrar(VentaRequestDTO dto) {

        // 1. Vendedor actual desde Spring Security
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario vendedor = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vendedor no encontrado"));

        // 2. Cliente opcional
        Cliente cliente = null;
        if (dto.getClienteId() != null) {
            cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));

            // 3. Verificar mayoría de edad
            if (!cliente.esMayorDeEdad()) {
                throw new ReglaDeNegocioException(
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
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con id: " + detalleDTO.getProductoId()));

            // 5. Validar stock disponible
            if (producto.getStock() < detalleDTO.getCantidad()) {
                throw new ReglaDeNegocioException(
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
        BigDecimal igvVenta = subtotalVenta.multiply(TarifasFiscales.IGV_RATE).setScale(2, RoundingMode.HALF_UP);
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
        registrarSalidasKardex(detalles, vendedor, ventaGuardada, "Venta: " + numeroComprobante);

        return toDTO(ventaGuardada);
    }

    @Override
    @Transactional
    public VentaResponseDTO registrarDesdeCarrito(List<ItemCarritoDTO> items, String metodoPago, Long clienteId,
                                                   BigDecimal costoEnvio, String distritoEnvio, String culqiChargeId) {

        if (items == null || items.isEmpty()) {
            throw new ReglaDeNegocioException("El carrito está vacío");
        }

        // Vendedor sistema que representa el canal web
        Usuario vendedor = usuarioRepository.findByUsername("tienda_online")
                .orElseThrow(() -> new RecursoNoEncontradoException(
                    "Usuario sistema 'tienda_online' no encontrado. " +
                    "Reinicia la aplicación para que sea creado automáticamente."));

        // Resolver cliente registrado (puede ser null para compras anónimas)
        Cliente cliente = null;
        if (clienteId != null) {
            cliente = clienteRepository.findById(clienteId).orElse(null);
        }

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal subtotalVenta = BigDecimal.ZERO;

        for (ItemCarritoDTO item : items) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new ReglaDeNegocioException(
                    "Stock insuficiente para \"" + producto.getNombre() + "\". " +
                    "Disponible: " + producto.getStock() +
                    ", solicitado: " + item.getCantidad());
            }

            // getSubtotal() ya descuenta promos de volumen / combo
            BigDecimal subtotalDetalle = item.getSubtotal().setScale(2, RoundingMode.HALF_UP);

            // Calcular % de descuento efectivo para registrar en el detalle
            BigDecimal bruto = item.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(item.getCantidad()));
            BigDecimal descuentoPct = bruto.compareTo(BigDecimal.ZERO) > 0
                    ? bruto.subtract(subtotalDetalle)
                           .divide(bruto, 4, RoundingMode.HALF_UP)
                           .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            detalles.add(DetalleVenta.builder()
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(item.getPrecioUnitario())
                    .descuentoPorcentaje(descuentoPct)
                    .subtotal(subtotalDetalle)
                    .build());

            subtotalVenta = subtotalVenta.add(subtotalDetalle);

            // Descontar stock inmediatamente
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
        }

        BigDecimal envio       = (costoEnvio != null) ? costoEnvio : BigDecimal.ZERO;
        BigDecimal igvVenta    = subtotalVenta.multiply(TarifasFiscales.IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalVenta  = subtotalVenta.add(igvVenta).add(envio).setScale(2, RoundingMode.HALF_UP);
        String     comprobante = generarNumeroComprobante(TipoComprobante.TICKET);

        MetodoPago mp;
        try {
            mp = MetodoPago.valueOf(metodoPago.toUpperCase());
        } catch (Exception e) {
            mp = MetodoPago.EFECTIVO;
        }

        // Pedido pagado con tarjeta → COMPLETADA; otros métodos → PENDIENTE
        EstadoVenta estado = (culqiChargeId != null && !culqiChargeId.isBlank())
                ? EstadoVenta.COMPLETADA : EstadoVenta.PENDIENTE;

        String obs = (cliente != null
                ? "Pedido web — " + cliente.getNombre() + " " + cliente.getApellido()
                : "Pedido web — Tienda online")
                + (distritoEnvio != null && !distritoEnvio.isBlank() ? " | Envío: " + distritoEnvio : "")
                + (culqiChargeId != null && !culqiChargeId.isBlank() ? " | Culqi: " + culqiChargeId : "");

        Venta venta = Venta.builder()
                .numeroComprobante(comprobante)
                .tipoComprobante(TipoComprobante.TICKET)
                .fechaHora(LocalDateTime.now())
                .subtotal(subtotalVenta)
                .igv(igvVenta)
                .costoEnvio(envio)
                .total(totalVenta)
                .estado(estado)
                .metodoPago(mp)
                .cliente(cliente)
                .vendedor(vendedor)
                .distritoEnvio(distritoEnvio)
                .culqiChargeId(culqiChargeId)
                .observaciones(obs)
                .build();

        for (DetalleVenta d : detalles) {
            d.setVenta(venta);
            venta.getDetalles().add(d);
        }

        Venta ventaGuardada = ventaRepository.save(venta);

        // Registrar salidas en Kardex
        registrarSalidasKardex(detalles, vendedor, ventaGuardada, "Venta web: " + comprobante);

        return toDTO(ventaGuardada);
    }

    @Override
    @Transactional
    public VentaResponseDTO crearDesdePedido(Pedido pedido) {
        Usuario vendedor = usuarioRepository.findByUsername("tienda_online")
                .orElseThrow(() -> new RecursoNoEncontradoException(
                    "Usuario sistema 'tienda_online' no encontrado."));

        List<DetalleVenta> detalles = new ArrayList<>();

        for (PedidoItem item : pedido.getItems()) {
            if (item.getProductoId() == null) continue;
            Producto producto = productoRepository.findById(item.getProductoId()).orElse(null);
            if (producto == null) continue;

            // PedidoItem.descuento es monetario → convertir a porcentaje para DetalleVenta
            BigDecimal bruto = item.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(item.getCantidad()));
            BigDecimal descuentoPct = BigDecimal.ZERO;
            if (item.getDescuento() != null
                    && item.getDescuento().compareTo(BigDecimal.ZERO) > 0
                    && bruto.compareTo(BigDecimal.ZERO) > 0) {
                descuentoPct = item.getDescuento()
                        .divide(bruto, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            detalles.add(DetalleVenta.builder()
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(item.getPrecioUnitario())
                    .descuentoPorcentaje(descuentoPct)
                    .subtotal(item.getSubtotal())
                    .build());
        }

        // Pedido.MetodoPago → Venta.MetodoPago (CONTRA_ENTREGA no existe en Venta → EFECTIVO)
        MetodoPago mp = MetodoPago.EFECTIVO;
        if (pedido.getMetodoPago() != null) {
            mp = switch (pedido.getMetodoPago()) {
                case TARJETA -> MetodoPago.TARJETA;
                case YAPE    -> MetodoPago.YAPE;
                case PLIN    -> MetodoPago.PLIN;
                default      -> MetodoPago.EFECTIVO;
            };
        }

        // Pedido.TipoComprobante → Venta.TipoComprobante
        TipoComprobante tc = (pedido.getTipoComprobante() == Pedido.TipoComprobante.FACTURA)
                ? TipoComprobante.FACTURA : TipoComprobante.BOLETA;

        // COMPLETADA si pagó online con tarjeta; PENDIENTE para contra-entrega/Yape/Plin
        EstadoVenta estado = (pedido.getCulqiChargeId() != null && !pedido.getCulqiChargeId().isBlank())
                ? EstadoVenta.COMPLETADA : EstadoVenta.PENDIENTE;

        String nombre = pedido.getNombreCliente() != null ? pedido.getNombreCliente() : "";
        String apellido = pedido.getApellidoCliente() != null ? pedido.getApellidoCliente() : "";
        String obs = "Pedido web " + pedido.getNumeroPedido() + " — " + nombre + " " + apellido
                + (pedido.getDistrito() != null && !pedido.getDistrito().isBlank()
                   ? " | Envío: " + pedido.getDistrito() : "");

        BigDecimal envio = pedido.getCostoEnvio() != null ? pedido.getCostoEnvio() : BigDecimal.ZERO;
        String comprobante = generarNumeroComprobante(tc);

        // Asociar la venta al cliente registrado cuando el pedido proviene de una cuenta web.
        // Esto es clave para que `/mi-cuenta/pedidos` (que consulta por clienteId) funcione.
        Cliente clienteAsociado = null;
        if (pedido.getClienteWeb() != null) {
            clienteAsociado = pedido.getClienteWeb().getCliente();
        }

        Venta venta = Venta.builder()
                .numeroComprobante(comprobante)
                .tipoComprobante(tc)
                .fechaHora(pedido.getPagadoEn() != null ? pedido.getPagadoEn() : LocalDateTime.now())
                .subtotal(pedido.getSubtotal())
                .igv(pedido.getIgv())
                .costoEnvio(envio)
                .total(pedido.getTotal())
                .estado(estado)
                .metodoPago(mp)
                .cliente(clienteAsociado)
                .vendedor(vendedor)
                .distritoEnvio(pedido.getDistrito())
                .culqiChargeId(pedido.getCulqiChargeId())
                .observaciones(obs)
                .build();

        for (DetalleVenta d : detalles) {
            d.setVenta(venta);
            venta.getDetalles().add(d);
        }

        Venta ventaGuardada = ventaRepository.save(venta);

        // Kardex: el stock ya fue decrementado por PedidoService → reconstruir stockAnterior
        registrarSalidasKardex(detalles, vendedor, ventaGuardada,
                "Venta web: " + comprobante + " | " + pedido.getNumeroPedido());

        return toDTO(ventaGuardada);
    }

    @Override
    @Transactional
    public VentaResponseDTO completar(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con id: " + id));

        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new ReglaDeNegocioException("Solo se pueden completar ventas en estado PENDIENTE");
        }

        venta.setEstado(EstadoVenta.COMPLETADA);
        return toDTO(ventaRepository.save(venta));
    }

    @Override
    @Transactional
    public VentaResponseDTO anular(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con id: " + id));

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new ReglaDeNegocioException("La venta ya se encuentra anulada");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

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
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con id: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO buscarPorComprobante(String numeroComprobante) {
        return toDTO(ventaRepository.findByNumeroComprobante(numeroComprobante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comprobante no encontrado: " + numeroComprobante)));
    }

    private String generarNumeroComprobante(TipoComprobante tipo) {
        return GeneradorCorrelativo.siguiente(tipo.name(), ventaRepository.findUltimoNumeroComprobante(tipo));
    }

    /** Registra una salida de Kardex (venta) por cada detalle, usando el stock ya actualizado del producto. */
    private void registrarSalidasKardex(List<DetalleVenta> detalles, Usuario vendedor, Venta ventaGuardada, String motivo) {
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
                    .motivo(motivo)
                    .venta(ventaGuardada)
                    .build());
        }
    }

    @Override
    public VentaResponseDTO toDTO(Venta v) {
        List<DetalleVentaResponseDTO> detallesDTO = v.getDetalles().stream() 
                .map(d-> DetalleVentaResponseDTO.builder()
                        .id(d.getId().longValue())
                        .productoNombre(d.getProducto().getNombre())
                        .productoCodigo(d.getProducto().getCodigo())
                        .productoMarca(d.getProducto().getMarca())
                        .productoCantidad(d.getProducto().getCantidad())
                        .productoUnidad(d.getProducto().getUnidadMedida())
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
                .costoEnvio(v.getCostoEnvio())
                .total(v.getTotal())
                .distritoEnvio(v.getDistritoEnvio())
                .observaciones(v.getObservaciones())
                .detalles(detallesDTO)
                .build();
    }
}
