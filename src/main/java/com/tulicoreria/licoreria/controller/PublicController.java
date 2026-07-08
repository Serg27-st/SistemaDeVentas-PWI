package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.ItemCarritoDTO;
import com.tulicoreria.licoreria.dto.ProductoResponseDTO;
import com.tulicoreria.licoreria.model.Promocion;
import com.tulicoreria.licoreria.service.BusquedaService;
import com.tulicoreria.licoreria.util.FuzzySearchUtil;
import com.tulicoreria.licoreria.service.CategoriaService;
import com.tulicoreria.licoreria.service.EnvioService;
import com.tulicoreria.licoreria.service.ProductoService;
import com.tulicoreria.licoreria.service.PromocionService;
import com.tulicoreria.licoreria.util.TarifasFiscales;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.*;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final BusquedaService busquedaService;
    private final PromocionService promocionService;
    private final EnvioService envioService;
    private final CarritoSessionHelper carritoSession;

    @org.springframework.beans.factory.annotation.Value("${app.carrito.pedido-minimo:50.00}")
    private BigDecimal pedidoMinimo;

    @ModelAttribute
    public void commonAttributes(Model model) {
        model.addAttribute("categorias", categoriaService.listarActivas());
    }

    @GetMapping("/inicio")
    public String inicio(Model model) {
        List<ProductoResponseDTO> destacados = productoService.listarTodos().stream()
                .filter(p -> !p.isSinStock())
                .limit(8)
                .toList();
        model.addAttribute("productosDestacados", destacados);

        List<Promocion> combosDestacados = promocionService.findCombosActivos().stream()
                .limit(4).toList();
        model.addAttribute("combosDestacados", combosDestacados);

        // Pre-computar totalNormal por combo para evitar lambdas Java en SpEL del template
        Map<Long, BigDecimal> totalesNormales = new LinkedHashMap<>();
        for (Promocion combo : combosDestacados) {
            BigDecimal total = combo.getItems().stream()
                    .filter(ci -> ci.getProducto() != null)
                    .map(ci -> ci.getProducto().getPrecioVenta()
                            .multiply(BigDecimal.valueOf(ci.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalesNormales.put(combo.getId(), total);
        }
        model.addAttribute("totalesNormales", totalesNormales);

        return "publica/inicio";
    }

    @GetMapping("/catalogo")
    public String catalogo(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoria,
            @RequestParam(defaultValue = "nombre") String orden,
            Model model) {

        List<ProductoResponseDTO> productos = productoService.listarTodos();

        if (q != null && !q.isBlank()) {
            // Búsqueda fuzzy multicriterio con tolerancia a errores tipográficos
            busquedaService.registrarBusqueda(q);
            final String term = q.trim();
            productos = productos.stream()
                    .filter(p -> FuzzySearchUtil.scoreProducto(p, term) >= FuzzySearchUtil.THRESHOLD)
                    .sorted((a, b) -> Double.compare(
                            FuzzySearchUtil.scoreProducto(b, term),
                            FuzzySearchUtil.scoreProducto(a, term)))
                    .toList();
        }

        String categoriaActual = null;
        if (categoria != null) {
            final Long catId = categoria;
            productos = productos.stream()
                    .filter(p -> catId.equals(p.getCategoriaId()))
                    .toList();
            try {
                categoriaActual = categoriaService.buscarPorId(catId).getNombre();
            } catch (Exception ignored) {}
        }

        Comparator<ProductoResponseDTO> comp = switch (orden) {
            case "precio_asc" -> Comparator.comparing(ProductoResponseDTO::getPrecioVenta);
            case "precio_desc" -> Comparator.comparing(ProductoResponseDTO::getPrecioVenta).reversed();
            default -> Comparator.comparing(p -> p.getNombre().toLowerCase(Locale.ROOT));
        };
        productos = productos.stream().sorted(comp).toList();

        model.addAttribute("productos", productos);
        model.addAttribute("busqueda", q);
        model.addAttribute("categoriaId", categoria);
        model.addAttribute("categoriaActual", categoriaActual);
        model.addAttribute("orden", orden);
        return "publica/catalogo";
    }

    @GetMapping("/producto/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        ProductoResponseDTO producto = productoService.buscarPorId(id);
        List<ProductoResponseDTO> relacionados = productoService.listarTodos().stream()
                .filter(p -> !p.getId().equals(id)
                        && producto.getCategoriaId() != null
                        && producto.getCategoriaId().equals(p.getCategoriaId())
                        && !p.isSinStock())
                .limit(4)
                .toList();
        model.addAttribute("producto", producto);
        model.addAttribute("relacionados", relacionados);
        return "publica/detalle-producto";
    }

    @GetMapping("/carrito")
    public String verCarrito(HttpSession session, Model model) {
        // Aplicar descuentos de volumen a los ítems del carrito
        List<ItemCarritoDTO> items = carritoSession.getCarrito(session).values().stream()
                .map(CarritoSessionHelper::copiarItem)
                .toList();
        promocionService.aplicarDescuentoVolumen(items);

        // Expandir combos en ítems visuales
        Map<Long, Integer> combosCarrito = carritoSession.getCombosCarrito(session);
        List<ItemCarritoDTO> comboItems  = promocionService.expandirCombos(combosCarrito);

        List<ItemCarritoDTO> todosLosItems = Stream.concat(items.stream(), comboItems.stream()).toList();

        BigDecimal subtotal = todosLosItems.stream()
                .map(ItemCarritoDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descuentoTotal = todosLosItems.stream()
                .filter(i -> i.getDescuentoAplicado() != null)
                .map(ItemCarritoDTO::getDescuentoAplicado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal igv   = subtotal.multiply(TarifasFiscales.IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);

        model.addAttribute("items", todosLosItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("descuentoTotal", descuentoTotal);
        model.addAttribute("igv", igv);
        model.addAttribute("total", total);
        model.addAttribute("tarifasEnvio", envioService.getTarifas());
        model.addAttribute("pedidoMinimo", pedidoMinimo);
        return "publica/carrito";
    }

    // ── Combos ────────────────────────────────────────────────────────────────

    @PostMapping("/carrito/agregar-combo")
    public String agregarCombo(@RequestParam Long comboId,
                               @RequestParam(defaultValue = "1") int cantidad,
                               HttpSession session,
                               RedirectAttributes flash) {
        try {
            Map<Long, Integer> combos = carritoSession.getCombosCarrito(session);
            combos.merge(comboId, cantidad, Integer::sum);
            flash.addFlashAttribute("carritoExito", "Pack agregado al carrito.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMensaje", "No se pudo agregar el pack.");
        }
        return "redirect:/combos";
    }

    @PostMapping("/carrito/eliminar-combo")
    public String eliminarCombo(@RequestParam Long promocionId, HttpSession session) {
        carritoSession.getCombosCarrito(session).remove(promocionId);
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/agregar")
    public String agregarAlCarrito(
            @RequestParam Long productoId,
            @RequestParam(defaultValue = "1") int cantidad,
            HttpSession session,
            RedirectAttributes flash) {
        try {
            ProductoResponseDTO p = productoService.buscarPorId(productoId);
            if (p.isSinStock()) {
                flash.addFlashAttribute("errorMensaje", "El producto está agotado.");
                return "redirect:/producto/" + productoId;
            }
            Map<Long, ItemCarritoDTO> carrito = carritoSession.getCarrito(session);
            if (carrito.containsKey(productoId)) {
                ItemCarritoDTO item = carrito.get(productoId);
                item.setCantidad(Math.min(item.getCantidad() + cantidad, p.getStock()));
            } else {
                carrito.put(productoId, ItemCarritoDTO.builder()
                        .productoId(productoId)
                        .nombre(p.getNombre())
                        .marca(p.getMarca())
                        .urlImagen(p.getUrlImagen())
                        .precioUnitario(p.getPrecioVenta())
                        .cantidad(Math.min(cantidad, p.getStock()))
                        .build());
            }
            flash.addFlashAttribute("carritoExito", p.getNombre() + " agregado al carrito.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMensaje", "No se pudo agregar el producto.");
        }
        return "redirect:/producto/" + productoId;
    }

    @PostMapping("/carrito/actualizar")
    public String actualizarCarrito(
            @RequestParam Long productoId,
            @RequestParam int cantidad,
            HttpSession session) {
        Map<Long, ItemCarritoDTO> carrito = carritoSession.getCarrito(session);
        if (cantidad <= 0) {
            carrito.remove(productoId);
        } else {
            carrito.computeIfPresent(productoId, (k, v) -> {
                v.setCantidad(cantidad);
                return v;
            });
        }
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/eliminar")
    public String eliminarDelCarrito(@RequestParam Long productoId, HttpSession session) {
        carritoSession.getCarrito(session).remove(productoId);
        return "redirect:/carrito";
    }
}
