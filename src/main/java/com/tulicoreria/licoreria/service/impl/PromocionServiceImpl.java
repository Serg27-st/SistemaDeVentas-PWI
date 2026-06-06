package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.dto.ItemCarritoDTO;
import com.tulicoreria.licoreria.dto.PromocionRequestDTO;
import com.tulicoreria.licoreria.dto.PromocionResponseDTO;
import com.tulicoreria.licoreria.dto.PromocionResponseDTO.ComboItemDTO;
import com.tulicoreria.licoreria.model.ComboItem;
import com.tulicoreria.licoreria.model.Producto;
import com.tulicoreria.licoreria.model.Promocion;
import com.tulicoreria.licoreria.model.Promocion.TipoPromocion;
import com.tulicoreria.licoreria.repository.PromocionRepository;
import com.tulicoreria.licoreria.repository.ProductoRepository;
import com.tulicoreria.licoreria.service.PromocionService;
import com.tulicoreria.licoreria.service.impl.ImagenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromocionServiceImpl implements PromocionService {

    private final PromocionRepository promocionRepository;
    private final ProductoRepository  productoRepository;
    private final ImagenService       imagenService;

    // ── Admin CRUD ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PromocionResponseDTO crear(PromocionRequestDTO dto) {
        Promocion p = buildFromDTO(new Promocion(), dto);
        return toDTO(promocionRepository.save(p));
    }

    @Override
    @Transactional
    public PromocionResponseDTO actualizar(Long id, PromocionRequestDTO dto) {
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada: " + id));
        buildFromDTO(p, dto);
        return toDTO(promocionRepository.save(p));
    }

    @Override
    @Transactional
    public void activar(Long id) {
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada: " + id));
        p.setActivo(true);
        promocionRepository.save(p);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada: " + id));
        p.setActivo(false);
        promocionRepository.save(p);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada: " + id));
        promocionRepository.delete(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> listarTodas() {
        return promocionRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionResponseDTO buscarPorId(Long id) {
        return toDTO(promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada: " + id)));
    }

    // ── Consultas públicas ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<Promocion> findPromocionActivaDirecta(Long productoId) {
        return promocionRepository.findActivaPorTipoYProducto(
                TipoPromocion.DESCUENTO_DIRECTO, productoId, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Promocion> findPromocionActivaVolumen(Long productoId) {
        return promocionRepository.findActivaPorTipoYProducto(
                TipoPromocion.VOLUMEN, productoId, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promocion> findCombosActivos() {
        return promocionRepository.findCombosActivos(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Promocion findComboById(Long id) {
        return promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Combo no encontrado: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Promocion> mapPromoDirectaActiva() {
        return buildMapPorProducto(TipoPromocion.DESCUENTO_DIRECTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Promocion> mapPromoVolumenActiva() {
        return buildMapPorProducto(TipoPromocion.VOLUMEN);
    }

    // ── Cálculo de precios ─────────────────────────────────────────────────────

    @Override
    public BigDecimal calcularPrecioConDescuento(BigDecimal precioBase, Promocion promo) {
        if (promo.getPorcentajeDescuento() != null) {
            BigDecimal factor = BigDecimal.ONE.subtract(
                    promo.getPorcentajeDescuento().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            return precioBase.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        }
        if (promo.getMontoDescuento() != null) {
            return precioBase.subtract(promo.getMontoDescuento()).max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return precioBase;
    }

    @Override
    @Transactional(readOnly = true)
    public void aplicarDescuentoVolumen(ItemCarritoDTO item) {
        Optional<Promocion> promoOpt =
                findPromocionActivaVolumen(item.getProductoId());
        if (promoOpt.isEmpty()) return;

        Promocion promo = promoOpt.get();
        int compraX = promo.getCompraX() != null ? promo.getCompraX() : 0;
        int llevaY  = promo.getLlevaY()  != null ? promo.getLlevaY()  : 0;
        if (compraX <= 0 || llevaY <= 0 || llevaY >= compraX) return;

        int cantidad = item.getCantidad();
        int grupos   = cantidad / compraX;
        int gratis   = grupos * (compraX - llevaY);

        if (gratis > 0) {
            BigDecimal descuento = item.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(gratis))
                    .setScale(2, RoundingMode.HALF_UP);
            item.setDescuentoAplicado(descuento);
            item.setEtiquetaPromo(compraX + "×" + llevaY + " — " + gratis + " gratis");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemCarritoDTO> expandirCombos(Map<Long, Integer> combosCarrito) {
        if (combosCarrito == null || combosCarrito.isEmpty()) return List.of();

        List<ItemCarritoDTO> resultado = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : combosCarrito.entrySet()) {
            Long   promocionId  = entry.getKey();
            int    cantidadPack = entry.getValue();
            Promocion combo = promocionRepository.findById(promocionId).orElse(null);
            if (combo == null || !combo.esActiva()) continue;

            // Calcular precio total sin descuento para distribuir proporcional
            BigDecimal totalNormal = combo.getItems().stream()
                    .map(ci -> ci.getProducto().getPrecioVenta()
                            .multiply(BigDecimal.valueOf(ci.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal precioCombo = combo.getPrecioCombo() != null
                    ? combo.getPrecioCombo() : totalNormal;

            BigDecimal ratio = totalNormal.compareTo(BigDecimal.ZERO) > 0
                    ? precioCombo.divide(totalNormal, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ONE;

            for (ComboItem ci : combo.getItems()) {
                Producto prod = ci.getProducto();
                BigDecimal precioPromo = prod.getPrecioVenta()
                        .multiply(ratio).setScale(2, RoundingMode.HALF_UP);

                ItemCarritoDTO item = ItemCarritoDTO.builder()
                        .productoId(prod.getId())
                        .nombre(prod.getNombre())
                        .marca(prod.getMarca())
                        .urlImagen(imagenService.construirUrl(prod.getImagen()))
                        .precioUnitario(precioPromo)
                        .cantidad(ci.getCantidad() * cantidadPack)
                        .etiquetaPromo("Pack: " + combo.getNombre())
                        .build();
                resultado.add(item);
            }
        }
        return resultado;
    }

    // ── toDTO ──────────────────────────────────────────────────────────────────

    @Override
    public PromocionResponseDTO toDTO(Promocion p) {
        List<ComboItemDTO> itemDTOs = p.getItems().stream()
                .map(ci -> ComboItemDTO.builder()
                        .productoId(ci.getProducto().getId())
                        .productoNombre(ci.getProducto().getNombre())
                        .productoMarca(ci.getProducto().getMarca())
                        .urlImagen(imagenService.construirUrl(ci.getProducto().getImagen()))
                        .precioVenta(ci.getProducto().getPrecioVenta())
                        .cantidad(ci.getCantidad())
                        .build())
                .toList();

        // Listas paralelas para el formulario de edición (combo)
        List<Long>    comboIds  = p.getItems().stream().map(ci -> ci.getProducto().getId()).toList();
        List<Integer> comboCant = p.getItems().stream().map(ComboItem::getCantidad).toList();

        return PromocionResponseDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .tipo(p.getTipo().name())
                .porcentajeDescuento(p.getPorcentajeDescuento())
                .montoDescuento(p.getMontoDescuento())
                .compraX(p.getCompraX())
                .llevaY(p.getLlevaY())
                .precioCombo(p.getPrecioCombo())
                .urlImagenCombo(imagenService.construirUrl(p.getImagenCombo()))
                .productoId(p.getProducto() != null ? p.getProducto().getId() : null)
                .productoNombre(p.getProducto() != null ? p.getProducto().getNombre() : null)
                .items(itemDTOs)
                .comboProductoIds(comboIds)
                .comboCantidades(comboCant)
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .activo(p.isActivo())
                .vigente(p.esActiva())
                .etiquetaCorta(p.etiquetaCorta())
                .build();
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private Promocion buildFromDTO(Promocion p, PromocionRequestDTO dto) {
        TipoPromocion tipo = TipoPromocion.valueOf(dto.getTipo());

        p.setNombre(dto.getNombre() != null ? dto.getNombre().trim() : "");
        p.setDescripcion(dto.getDescripcion());
        p.setTipo(tipo);
        p.setFechaInicio(dto.getFechaInicio());
        p.setFechaFin(dto.getFechaFin());
        p.setActivo(dto.isActivo());

        // ── Limpiar todos los campos específicos y solo rellenar los del tipo activo
        p.setPorcentajeDescuento(null);
        p.setMontoDescuento(null);
        p.setCompraX(null);
        p.setLlevaY(null);
        p.setPrecioCombo(null);
        p.setProducto(null);

        switch (tipo) {
            case DESCUENTO_DIRECTO -> {
                // Solo uno de los dos debe enviarse; prevalece porcentaje si ambos llegan
                if (dto.getPorcentajeDescuento() != null) {
                    p.setPorcentajeDescuento(dto.getPorcentajeDescuento());
                } else {
                    p.setMontoDescuento(dto.getMontoDescuento());
                }
                if (dto.getProductoId() != null) {
                    productoRepository.findById(dto.getProductoId()).ifPresent(p::setProducto);
                }
            }
            case VOLUMEN -> {
                p.setCompraX(dto.getCompraX());
                p.setLlevaY(dto.getLlevaY());
                if (dto.getProductoId() != null) {
                    productoRepository.findById(dto.getProductoId()).ifPresent(p::setProducto);
                }
            }
            case COMBO -> {
                p.setPrecioCombo(dto.getPrecioCombo());
                // Imagen del combo
                if (dto.getImagenComboFile() != null && !dto.getImagenComboFile().isEmpty()) {
                    String ruta = imagenService.guardarImagenProducto("combos", dto.getImagenComboFile());
                    p.setImagenCombo(ruta);
                }
                // Ítems del combo
                p.getItems().clear();
                List<Long>    ids  = dto.getComboProductoIds();
                List<Integer> ctds = dto.getComboCantidades();
                if (ids != null) {
                    for (int i = 0; i < ids.size(); i++) {
                        Long pId = ids.get(i);
                        if (pId == null) continue;
                        int qty = (ctds != null && i < ctds.size() && ctds.get(i) != null)
                                ? ctds.get(i) : 1;
                        productoRepository.findById(pId).ifPresent(prod ->
                            p.getItems().add(ComboItem.builder()
                                    .promocion(p).producto(prod).cantidad(qty).build())
                        );
                    }
                }
            }
        }
        return p;
    }

    private Map<Long, Promocion> buildMapPorProducto(TipoPromocion tipo) {
        LocalDate hoy = LocalDate.now();
        return promocionRepository.findTodasActivas(hoy).stream()
                .filter(p -> p.getTipo() == tipo && p.getProducto() != null)
                .collect(Collectors.toMap(
                        p -> p.getProducto().getId(),
                        p -> p,
                        (a, b) -> a));
    }
}
