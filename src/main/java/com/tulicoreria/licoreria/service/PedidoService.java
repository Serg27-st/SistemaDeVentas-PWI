package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.DatosEnvioDTO;
import com.tulicoreria.licoreria.dto.ItemCarritoDTO;
import com.tulicoreria.licoreria.model.Pedido;

import java.util.List;
import java.util.Map;

public interface PedidoService {

    /**
     * Crea un Pedido con estado PENDIENTE_PAGO a partir del carrito.
     * No descuenta stock todavía — lo hace en {@link #confirmarPago}.
     *
     * @param items          ítems del carrito (ya con descuentos aplicados)
     * @param combosCarrito  mapa comboId→cantidad del carrito de combos
     * @param clienteWebId   null para invitados
     * @param emailInvitado  email ingresado si el usuario no está registrado
     * @return Pedido creado y persistido
     */
    Pedido crear(List<ItemCarritoDTO> items, Map<Long, Integer> combosCarrito,
                 Long clienteWebId, String emailInvitado);

    /** Actualiza la Pantalla 2 (datos de envío) y calcula el costo de delivery. */
    Pedido actualizarDatosEnvio(Long pedidoId, DatosEnvioDTO dto);

    /**
     * Pantalla 3 — pago con tarjeta (Culqi):
     * <ol>
     *   <li>Valida stock nuevamente (puede haber cambiado)</li>
     *   <li>Cobra via CulqiService</li>
     *   <li>Descuenta stock</li>
     *   <li>Marca pedido como PAGADO</li>
     * </ol>
     */
    Pedido confirmarPagoOnline(Long pedidoId, String culqiToken, String email);

    /**
     * Pantalla 3 — contra entrega:
     * <ol>
     *   <li>Valida stock</li>
     *   <li>Descuenta stock</li>
     *   <li>Marca pedido como POR_ENTREGAR</li>
     * </ol>
     *
     * @param metodoPago CONTRA_ENTREGA | YAPE | PLIN
     */
    Pedido confirmarContraEntrega(Long pedidoId, String metodoPago);

    /** Cancela el pedido y restaura el stock si fue reservado. */
    void cancelar(Long pedidoId, String motivo);

    Pedido buscarPorId(Long pedidoId);

    /** Libera stock de pedidos expirados (llamar desde un @Scheduled). */
    void liberarPedidosExpirados();
}
