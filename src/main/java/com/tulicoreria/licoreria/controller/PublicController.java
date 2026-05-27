package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.ItemCarritoDTO;
import com.tulicoreria.licoreria.dto.ProductoResponseDTO;
import com.tulicoreria.licoreria.service.CategoriaService;
import com.tulicoreria.licoreria.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    private static final String CARRITO_KEY = "carrito";
    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

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
            String term = q.toLowerCase(Locale.ROOT);
            productos = productos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(term)
                            || (p.getMarca() != null && p.getMarca().toLowerCase().contains(term))
                            || (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(term)))
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
        Map<Long, ItemCarritoDTO> carrito = getCarrito(session);
        List<ItemCarritoDTO> items = new ArrayList<>(carrito.values());

        BigDecimal subtotal = items.stream()
                .map(ItemCarritoDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal igv = subtotal.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);

        model.addAttribute("items", items);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("igv", igv);
        model.addAttribute("total", total);
        return "publica/carrito";
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
            Map<Long, ItemCarritoDTO> carrito = getCarrito(session);
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
        Map<Long, ItemCarritoDTO> carrito = getCarrito(session);
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
        getCarrito(session).remove(productoId);
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/confirmar")
    public String confirmarPedido(
            @RequestParam String metodoPago,
            HttpSession session,
            RedirectAttributes flash) {
        Map<Long, ItemCarritoDTO> carrito = getCarrito(session);
        if (carrito.isEmpty()) {
            flash.addFlashAttribute("errorMensaje", "Tu carrito está vacío.");
            return "redirect:/carrito";
        }
        session.removeAttribute(CARRITO_KEY);
        flash.addFlashAttribute("pedidoConfirmado", true);
        flash.addFlashAttribute("metodoPago", metodoPago);
        return "redirect:/carrito/confirmacion";
    }

    @GetMapping("/carrito/confirmacion")
    public String confirmacion() {
        return "publica/confirmacion";
    }

    @SuppressWarnings("unchecked")
    private Map<Long, ItemCarritoDTO> getCarrito(HttpSession session) {
        Map<Long, ItemCarritoDTO> c = (Map<Long, ItemCarritoDTO>) session.getAttribute(CARRITO_KEY);
        if (c == null) {
            c = new LinkedHashMap<>();
            session.setAttribute(CARRITO_KEY, c);
        }
        return c;
    }
}
