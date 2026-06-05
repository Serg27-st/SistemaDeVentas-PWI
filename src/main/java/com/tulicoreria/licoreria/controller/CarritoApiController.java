package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.ItemCarritoDTO;
import com.tulicoreria.licoreria.dto.ProductoResponseDTO;
import com.tulicoreria.licoreria.service.ProductoService;
import com.tulicoreria.licoreria.service.PromocionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Stream;

/**
 * REST API para operaciones AJAX del carrito de compras.
 * Todos los endpoints devuelven JSON y no recargan la página.
 */
@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoApiController {

    private final ProductoService  productoService;
    private final PromocionService promocionService;

    private static final String CARRITO_KEY  = "carrito";
    private static final String COMBOS_KEY   = "combosCarrito";
    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    @Value("${app.carrito.pedido-minimo:50.00}")
    private BigDecimal pedidoMinimo;

    // ── Actualizar cantidad ─────────────────────────────────────────────────

    @PostMapping("/actualizar")
    public ResponseEntity<Map<String, Object>> actualizar(
            @RequestParam Long productoId,
            @RequestParam int cantidad,
            HttpSession session) {

        Map<Long, ItemCarritoDTO> carrito = getCarrito(session);
        Map<String, Object> resp = new LinkedHashMap<>();

        try {
            ProductoResponseDTO prod = productoService.buscarPorId(productoId);
            int stockReal = prod.getStock();

            if (cantidad <= 0) {
                carrito.remove(productoId);
            } else {
                ItemCarritoDTO item = carrito.computeIfAbsent(productoId,
                    k -> ItemCarritoDTO.builder()
                            .productoId(productoId)
                            .nombre(prod.getNombre())
                            .marca(prod.getMarca())
                            .urlImagen(prod.getUrlImagen())
                            .precioUnitario(prod.getPrecioVenta())
                            .build());
                item.setCantidad(cantidad);
            }

            // Calcular subtotal del ítem con promos
            BigDecimal subtotalItem = BigDecimal.ZERO;
            BigDecimal descuentoItem = BigDecimal.ZERO;
            boolean stockInsuficiente = false;
            String mensajeStock = null;

            if (cantidad > 0 && carrito.containsKey(productoId)) {
                ItemCarritoDTO copia = copiarItem(carrito.get(productoId));
                promocionService.aplicarDescuentoVolumen(copia);
                subtotalItem  = copia.getSubtotal();
                descuentoItem = copia.getDescuentoAplicado() != null ? copia.getDescuentoAplicado() : BigDecimal.ZERO;

                if (cantidad > stockReal) {
                    stockInsuficiente = true;
                    mensajeStock = "Solo quedan " + stockReal + " unidades en almacén";
                }
            }

            Map<String, Object> totales = calcularTotales(session);

            resp.put("ok", true);
            resp.put("subtotalItem",    subtotalItem);
            resp.put("descuentoItem",   descuentoItem);
            resp.put("stockDisponible", prod.getStock());
            resp.put("stockInsuficiente", stockInsuficiente);
            resp.put("mensajeStock",    mensajeStock);
            resp.putAll(totales);

        } catch (Exception e) {
            resp.put("ok", false);
            resp.put("error", e.getMessage());
        }

        return ResponseEntity.ok(resp);
    }

    // ── Eliminar ítem ───────────────────────────────────────────────────────

    @PostMapping("/eliminar")
    public ResponseEntity<Map<String, Object>> eliminar(
            @RequestParam Long productoId,
            HttpSession session) {

        getCarrito(session).remove(productoId);
        Map<String, Object> totales = calcularTotales(session);
        totales.put("ok", true);
        return ResponseEntity.ok(totales);
    }

    // ── Validar stock de todos los ítems ────────────────────────────────────

    @GetMapping("/validar")
    public ResponseEntity<Map<String, Object>> validar(HttpSession session) {
        Map<Long, ItemCarritoDTO> carrito = getCarrito(session);
        List<Map<String, Object>> problemas = new ArrayList<>();

        for (ItemCarritoDTO item : carrito.values()) {
            try {
                ProductoResponseDTO prod = productoService.buscarPorId(item.getProductoId());
                if (item.getCantidad() > prod.getStock()) {
                    Map<String, Object> p = new LinkedHashMap<>();
                    p.put("productoId",         item.getProductoId());
                    p.put("nombre",             item.getNombre());
                    p.put("cantidadSolicitada", item.getCantidad());
                    p.put("stockDisponible",    prod.getStock());
                    p.put("mensaje", "Solo quedan " + prod.getStock() + " unidades");
                    problemas.add(p);
                }
            } catch (Exception ignored) {}
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("valido", problemas.isEmpty());
        resp.put("problemas", problemas);
        return ResponseEntity.ok(resp);
    }

    // ── Sugerencias de cross-selling ────────────────────────────────────────

    @GetMapping("/sugerencias")
    public ResponseEntity<List<Map<String, Object>>> sugerencias(HttpSession session) {
        Map<Long, ItemCarritoDTO> carrito = getCarrito(session);
        Set<Long>  enCarrito = carrito.keySet();

        // Palabras clave de los ítems en el carrito
        String nombresCarrito = carrito.values().stream()
                .map(i -> i.getNombre().toLowerCase())
                .reduce("", (a, b) -> a + " " + b);

        // Palabras clave objetivo según lo que hay en el carrito
        List<String> targetKeywords = new ArrayList<>();
        if (containsAny(nombresCarrito, "pisco", "gin", "vodka", "singani"))
            targetKeywords.addAll(List.of("ginger", "tónica", "tonica", "limonada", "hielo", "limón"));
        if (containsAny(nombresCarrito, "whisky", "whiskey", "bourbon", "ron", "brandy"))
            targetKeywords.addAll(List.of("cola", "hielo", "ginger", "soda"));
        if (containsAny(nombresCarrito, "cerveza", "beer", "pilsen", "cristal"))
            targetKeywords.addAll(List.of("snack", "maní", "piqueo", "chifle", "chicha"));
        if (containsAny(nombresCarrito, "vino", "wine", "espumante", "cava"))
            targetKeywords.addAll(List.of("queso", "uva", "copa"));

        List<ProductoResponseDTO> todos = productoService.listarTodos();

        // Si hay keywords objetivo, filtrar por ellos; si no, mostrar los más baratos no en carrito
        List<ProductoResponseDTO> candidatos;
        if (!targetKeywords.isEmpty()) {
            candidatos = todos.stream()
                    .filter(p -> !enCarrito.contains(p.getId()))
                    .filter(p -> !p.isSinStock())
                    .filter(p -> containsAny(p.getNombre().toLowerCase(), targetKeywords.toArray(new String[0])))
                    .limit(4)
                    .toList();
        } else {
            candidatos = List.of();
        }

        // Fallback: completar hasta 4 sugerencias con productos no en carrito
        final List<ProductoResponseDTO> candidatosFinal;
        if (candidatos.size() < 4) {
            final List<ProductoResponseDTO> base = candidatos;
            Set<Long> idsBase = base.stream().map(ProductoResponseDTO::getId)
                    .collect(java.util.stream.Collectors.toSet());
            List<ProductoResponseDTO> extra = todos.stream()
                    .filter(p -> !enCarrito.contains(p.getId()))
                    .filter(p -> !p.isSinStock())
                    .filter(p -> !idsBase.contains(p.getId()))
                    .sorted(Comparator.comparing(ProductoResponseDTO::getPrecioVenta))
                    .limit(4 - base.size())
                    .toList();
            candidatosFinal = Stream.concat(base.stream(), extra.stream()).toList();
        } else {
            candidatosFinal = candidatos;
        }

        // Convertir a mapa ligero (sin campos sensibles como precioCompra)
        List<Map<String, Object>> resultado = candidatosFinal.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",           p.getId());
            m.put("nombre",       p.getNombre());
            m.put("marca",        p.getMarca());
            m.put("precioVenta",  p.getPrecioVenta());
            m.put("urlImagen",    p.getUrlImagen() != null ? p.getUrlImagen() : "/images/producto-default.png");
            m.put("etiquetaPromo", p.getEtiquetaPromo());
            return m;
        }).toList();

        return ResponseEntity.ok(resultado);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> calcularTotales(HttpSession session) {
        Map<Long, ItemCarritoDTO> carrito = getCarrito(session);

        List<ItemCarritoDTO> items = carrito.values().stream().map(orig -> {
            ItemCarritoDTO copia = copiarItem(orig);
            promocionService.aplicarDescuentoVolumen(copia);
            return copia;
        }).toList();

        Map<Long, Integer> combosCarrito = getCombosCarrito(session);
        List<ItemCarritoDTO> comboItems  = promocionService.expandirCombos(combosCarrito);

        List<ItemCarritoDTO> todos = Stream.concat(items.stream(), comboItems.stream()).toList();

        BigDecimal subtotal       = todos.stream().map(ItemCarritoDTO::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descuentoTotal = todos.stream()
                .filter(i -> i.getDescuentoAplicado() != null)
                .map(ItemCarritoDTO::getDescuentoAplicado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal igv   = subtotal.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);

        // Progreso para pedido mínimo
        double progreso = pedidoMinimo.compareTo(BigDecimal.ZERO) > 0
                ? subtotal.divide(pedidoMinimo, 4, RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100))
                          .min(BigDecimal.valueOf(100))
                          .doubleValue()
                : 100.0;
        BigDecimal faltante = pedidoMinimo.subtract(subtotal).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("subtotal",        subtotal);
        m.put("descuentoTotal",  descuentoTotal);
        m.put("igv",             igv);
        m.put("total",           total);
        m.put("itemCount",       carrito.size() + combosCarrito.size());
        m.put("pedidoMinimo",    pedidoMinimo);
        m.put("progresoMinimo",  progreso);
        m.put("faltanteMinimo",  faltante);
        m.put("cumpleMinimo",    subtotal.compareTo(pedidoMinimo) >= 0);
        return m;
    }

    private static ItemCarritoDTO copiarItem(ItemCarritoDTO orig) {
        return ItemCarritoDTO.builder()
                .productoId(orig.getProductoId())
                .nombre(orig.getNombre())
                .marca(orig.getMarca())
                .urlImagen(orig.getUrlImagen())
                .precioUnitario(orig.getPrecioUnitario())
                .cantidad(orig.getCantidad())
                .build();
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) if (text.contains(kw)) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, ItemCarritoDTO> getCarrito(HttpSession session) {
        Map<Long, ItemCarritoDTO> c = (Map<Long, ItemCarritoDTO>) session.getAttribute(CARRITO_KEY);
        if (c == null) { c = new LinkedHashMap<>(); session.setAttribute(CARRITO_KEY, c); }
        return c;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCombosCarrito(HttpSession session) {
        Map<Long, Integer> c = (Map<Long, Integer>) session.getAttribute(COMBOS_KEY);
        if (c == null) { c = new LinkedHashMap<>(); session.setAttribute(COMBOS_KEY, c); }
        return c;
    }
}