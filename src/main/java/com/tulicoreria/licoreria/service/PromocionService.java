package com.tulicoreria.licoreria.service;

import com.tulicoreria.licoreria.dto.ItemCarritoDTO;
import com.tulicoreria.licoreria.dto.PromocionRequestDTO;
import com.tulicoreria.licoreria.dto.PromocionResponseDTO;
import com.tulicoreria.licoreria.model.Promocion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PromocionService {

    // ── Admin CRUD ────────────────────────────────────────────────────────────
    PromocionResponseDTO crear(PromocionRequestDTO dto);
    PromocionResponseDTO actualizar(Long id, PromocionRequestDTO dto);
    void desactivar(Long id);
    List<PromocionResponseDTO> listarTodas();
    PromocionResponseDTO buscarPorId(Long id);

    // ── Consultas públicas ────────────────────────────────────────────────────
    Optional<Promocion> findPromocionActivaDirecta(Long productoId);
    Optional<Promocion> findPromocionActivaVolumen(Long productoId);
    List<Promocion> findCombosActivos();
    Promocion findComboById(Long id);

    /**
     * Construye mapas de producto → promoción activa para todos los productos
     * (evita N+1 al listar el catálogo).
     */
    Map<Long, Promocion> mapPromoDirectaActiva();
    Map<Long, Promocion> mapPromoVolumenActiva();

    // ── Cálculo de precios ────────────────────────────────────────────────────
    BigDecimal calcularPrecioConDescuento(BigDecimal precioBase, Promocion promo);

    /**
     * Aplica el descuento por volumen sobre el ItemCarritoDTO (muta el objeto).
     * No hace nada si el producto no tiene promo de volumen activa.
     */
    void aplicarDescuentoVolumen(ItemCarritoDTO item);

    /** Expande los combos del carrito en ItemCarritoDTO individuales con precio proporcional. */
    List<ItemCarritoDTO> expandirCombos(Map<Long, Integer> combosCarrito);

    PromocionResponseDTO toDTO(Promocion p);
}
