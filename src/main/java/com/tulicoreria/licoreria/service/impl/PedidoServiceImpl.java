package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.dto.DatosEnvioDTO;
import com.tulicoreria.licoreria.dto.ItemCarritoDTO;
import com.tulicoreria.licoreria.model.*;
import com.tulicoreria.licoreria.model.Pedido.EstadoPedido;
import com.tulicoreria.licoreria.model.Pedido.MetodoPago;
import com.tulicoreria.licoreria.model.Pedido.TipoComprobante;
import com.tulicoreria.licoreria.repository.ClienteWebRepository;
import com.tulicoreria.licoreria.repository.PedidoRepository;
import com.tulicoreria.licoreria.repository.ProductoRepository;
import com.tulicoreria.licoreria.service.CulqiService;
import com.tulicoreria.licoreria.service.EnvioService;
import com.tulicoreria.licoreria.service.NotificacionPedidoService;
import com.tulicoreria.licoreria.service.PedidoService;
import com.tulicoreria.licoreria.service.PromocionService;
import com.tulicoreria.licoreria.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoServiceImpl.class);
    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");
    private static final int MINUTOS_EXPIRACION = 30;

    private final PedidoRepository          pedidoRepository;
    private final ProductoRepository         productoRepository;
    private final ClienteWebRepository       clienteWebRepository;
    private final PromocionService           promocionService;
    private final CulqiService               culqiService;
    private final EnvioService               envioService;
    private final NotificacionPedidoService  notificaciones;
    private final VentaService               ventaService;

    // ── Crear pedido ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Pedido crear(List<ItemCarritoDTO> items,
                        Map<Long, Integer> combosCarrito,
                        Long clienteWebId, String emailInvitado) {

        if (items == null || items.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // Expandir combos
        List<ItemCarritoDTO> comboItems = promocionService.expandirCombos(combosCarrito);
        List<ItemCarritoDTO> todosLosItems =
                Stream.concat(items.stream(), comboItems.stream()).toList();

        // Validar stock
        for (ItemCarritoDTO it : todosLosItems) {
            if (it.getProductoId() == null) continue;
            Producto prod = productoRepository.findById(it.getProductoId()).orElse(null);
            if (prod != null && prod.getStock() < it.getCantidad()) {
                throw new RuntimeException(
                    "Stock insuficiente para \"" + it.getNombre() + "\". " +
                    "Disponible: " + prod.getStock() + ", solicitado: " + it.getCantidad());
            }
        }

        // Calcular totales
        BigDecimal subtotal = todosLosItems.stream()
                .map(ItemCarritoDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal igv   = subtotal.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv).setScale(2, RoundingMode.HALF_UP);

        // Resolver cliente web
        ClienteWeb cw = (clienteWebId != null)
                ? clienteWebRepository.findById(clienteWebId).orElse(null)
                : null;

        String email = (cw != null) ? cw.getEmail() : emailInvitado;
        String nombre = (cw != null) ? cw.getNombre() : null;
        String apellido = (cw != null) ? cw.getApellido() : null;
        String telefono = (cw != null) ? cw.getTelefono() : null;

        // Construir pedido
        Pedido pedido = Pedido.builder()
                .numeroPedido(generarNumero())
                .estado(EstadoPedido.PENDIENTE_PAGO)
                .subtotal(subtotal)
                .igv(igv)
                .costoEnvio(BigDecimal.ZERO)
                .total(total)
                .clienteWeb(cw)
                .emailCliente(email)
                .nombreCliente(nombre)
                .apellidoCliente(apellido)
                .telefonoCliente(telefono)
                .tipoComprobante(TipoComprobante.BOLETA)
                .creadoEn(LocalDateTime.now())
                .expiraEn(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION))
                .build();

        // Añadir ítems
        for (ItemCarritoDTO it : todosLosItems) {
            BigDecimal desc = it.getDescuentoAplicado() != null
                    ? it.getDescuentoAplicado() : BigDecimal.ZERO;
            PedidoItem pi = PedidoItem.builder()
                    .pedido(pedido)
                    .productoId(it.getProductoId())
                    .productoNombre(it.getNombre())
                    .productoMarca(it.getMarca())
                    .etiquetaPromo(it.getEtiquetaPromo())
                    .precioUnitario(it.getPrecioUnitario())
                    .cantidad(it.getCantidad())
                    .descuento(desc.setScale(2, RoundingMode.HALF_UP))
                    .subtotal(it.getSubtotal().setScale(2, RoundingMode.HALF_UP))
                    .build();
            pedido.getItems().add(pi);
        }

        return pedidoRepository.save(pedido);
    }

    // ── Actualizar datos de envío ────────────────────────────────────────────

    @Override
    @Transactional
    public Pedido actualizarDatosEnvio(Long pedidoId, DatosEnvioDTO dto) {
        Pedido pedido = buscarPorId(pedidoId);
        verificarNoExpirado(pedido);

        BigDecimal cEnvio = envioService.getCosto(dto.getDistrito());

        pedido.setNombreCliente(dto.getNombre());
        pedido.setApellidoCliente(dto.getApellido());
        pedido.setEmailCliente(dto.getEmail());
        pedido.setTelefonoCliente(dto.getTelefono());
        pedido.setDniCliente(dto.getDni());
        pedido.setDireccion(dto.getDireccion());
        pedido.setDepartamento(dto.getDepartamento());
        pedido.setReferencia(dto.getReferencia());
        pedido.setDistrito(dto.getDistrito());
        pedido.setCostoEnvio(cEnvio);

        TipoComprobante tc = "FACTURA".equalsIgnoreCase(dto.getTipoComprobante())
                ? TipoComprobante.FACTURA : TipoComprobante.BOLETA;
        pedido.setTipoComprobante(tc);
        pedido.setRucCliente(dto.getRuc());
        pedido.setRazonSocial(dto.getRazonSocial());

        // Recalcular total con envío
        BigDecimal total = pedido.getSubtotal().add(pedido.getIgv())
                .add(cEnvio).setScale(2, RoundingMode.HALF_UP);
        pedido.setTotal(total);

        return pedidoRepository.save(pedido);
    }

    // ── Confirmar pago online ────────────────────────────────────────────────

    @Override
    @Transactional
    public Pedido confirmarPagoOnline(Long pedidoId, String culqiToken, String emailPagador) {
        Pedido pedido = buscarPorId(pedidoId);
        verificarNoExpirado(pedido);

        // Re-validar stock antes de cobrar
        validarStock(pedido);

        // Cobrar via Culqi
        String chargeId;
        try {
            String email = emailPagador != null && !emailPagador.isBlank()
                    ? emailPagador
                    : (pedido.getEmailCliente() != null ? pedido.getEmailCliente() : "cliente@tulicoreria.com");
            chargeId = culqiService.cobrar(culqiToken, pedido.getTotal(), email,
                    "Pedido " + pedido.getNumeroPedido());
        } catch (RuntimeException e) {
            pedido.setErrorPago(e.getMessage());
            pedidoRepository.save(pedido);
            throw e;   // el controller lo captura y redirige a /error
        }

        // Descontar stock
        descontarStock(pedido);

        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setMetodoPago(MetodoPago.TARJETA);
        pedido.setCulqiChargeId(chargeId);
        pedido.setPagadoEn(LocalDateTime.now());

        Pedido guardado = pedidoRepository.save(pedido);
        notificaciones.enviarConfirmacion(guardado);
        // Registrar en el sistema de ventas del admin
        ventaService.crearDesdePedido(guardado);
        return guardado;
    }

    // ── Confirmar contra entrega ─────────────────────────────────────────────

    @Override
    @Transactional
    public Pedido confirmarContraEntrega(Long pedidoId, String metodoPago) {
        Pedido pedido = buscarPorId(pedidoId);
        verificarNoExpirado(pedido);
        validarStock(pedido);
        descontarStock(pedido);

        MetodoPago mp;
        try { mp = MetodoPago.valueOf(metodoPago.toUpperCase()); }
        catch (Exception e) { mp = MetodoPago.CONTRA_ENTREGA; }

        pedido.setEstado(EstadoPedido.POR_ENTREGAR);
        pedido.setMetodoPago(mp);
        pedido.setPagadoEn(LocalDateTime.now());

        Pedido guardado = pedidoRepository.save(pedido);
        notificaciones.enviarConfirmacion(guardado);
        // Registrar en el sistema de ventas del admin
        ventaService.crearDesdePedido(guardado);
        return guardado;
    }

    // ── Cancelar ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void cancelar(Long pedidoId, String motivo) {
        Pedido pedido = buscarPorId(pedidoId);
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setErrorPago(motivo);
        pedidoRepository.save(pedido);
    }

    // ── Buscar ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));
    }

    // ── Liberar expirados ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void liberarPedidosExpirados() {
        List<Pedido> expirados = pedidoRepository.findExpirados(LocalDateTime.now());
        for (Pedido p : expirados) {
            p.setEstado(EstadoPedido.CANCELADO);
            p.setErrorPago("Expirado por tiempo de pago");
            log.info("Pedido {} cancelado por expiración", p.getNumeroPedido());
        }
        if (!expirados.isEmpty()) pedidoRepository.saveAll(expirados);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validarStock(Pedido pedido) {
        for (PedidoItem item : pedido.getItems()) {
            if (item.getProductoId() == null) continue;
            Producto prod = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));
            if (prod.getStock() < item.getCantidad()) {
                throw new RuntimeException(
                    "Stock insuficiente para \"" + item.getProductoNombre() + "\". " +
                    "Solo quedan " + prod.getStock() + " unidades.");
            }
        }
    }

    private void descontarStock(Pedido pedido) {
        for (PedidoItem item : pedido.getItems()) {
            if (item.getProductoId() == null) continue;
            productoRepository.findById(item.getProductoId()).ifPresent(prod -> {
                prod.setStock(Math.max(0, prod.getStock() - item.getCantidad()));
                productoRepository.save(prod);
            });
        }
    }

    private void verificarNoExpirado(Pedido pedido) {
        if (pedido.getExpiraEn() != null
                && LocalDateTime.now().isAfter(pedido.getExpiraEn())
                && pedido.getEstado() == EstadoPedido.PENDIENTE_PAGO) {
            cancelar(pedido.getId(), "Expirado por tiempo de pago");
            throw new RuntimeException("Tu sesión de pago expiró. Vuelve al carrito para intentarlo de nuevo.");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new RuntimeException("Este pedido fue cancelado. Regresa al carrito para iniciar uno nuevo.");
        }
    }

    private String generarNumero() {
        String ultimo = pedidoRepository.findUltimoNumeroPedido();
        int siguiente = 1;
        if (ultimo != null && ultimo.contains("-")) {
            try { siguiente = Integer.parseInt(ultimo.split("-")[1]) + 1; }
            catch (NumberFormatException ignored) {}
        }
        return "PED-" + String.format("%06d", siguiente);
    }
}
