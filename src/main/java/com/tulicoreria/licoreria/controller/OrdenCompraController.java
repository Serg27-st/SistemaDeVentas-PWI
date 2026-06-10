package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.OrdenCompraRequestDTO;
import com.tulicoreria.licoreria.service.OrdenCompraService;
import com.tulicoreria.licoreria.service.ProductoService;
import com.tulicoreria.licoreria.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ordenes")
@RequiredArgsConstructor
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;
    private final ProveedorService proveedorService;
    private final ProductoService productoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ordenes", ordenCompraService.listarTodas());
        model.addAttribute("pendientes", ordenCompraService.listarPendientes());
        return "ordenes/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("orden", ordenCompraService.buscarPorId(id));
        return "ordenes/detalle";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasAnyRole('ADMIN','ALMACEN')")
    public String nuevaForm(Model model) {
        model.addAttribute("ordenRequest", new OrdenCompraRequestDTO());
        model.addAttribute("proveedores", proveedorService.listarActivos());
        model.addAttribute("productos", productoService.listarTodos());
        return "ordenes/formulario";
    }

    @PostMapping("/nueva")
    @PreAuthorize("hasAnyRole('ADMIN','ALMACEN')")
    public String crear(@ModelAttribute OrdenCompraRequestDTO dto,
                        RedirectAttributes flash) {
        try {
            var orden = ordenCompraService.crear(dto);
            flash.addFlashAttribute("exito",
                "Orden de compra creada: " + orden.getNumeroOrden());
            return "redirect:/ordenes/" + orden.getId();
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/ordenes/nueva";
        }
    }

    // Recibir mercadería — actualiza stock y Kardex
    @PostMapping("/{id}/recibir")
    @PreAuthorize("hasAnyRole('ADMIN','ALMACEN')")
    public String recibirMercaderia(@PathVariable Long id,
                                    @RequestParam String comprobanteProveedor,
                                    RedirectAttributes flash) {
        try {
            ordenCompraService.recibirMercaderia(id, comprobanteProveedor);
            flash.addFlashAttribute("exito",
                "Mercadería recibida. Stock actualizado correctamente.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ordenes/" + id;
    }

    // Anular orden — solo si está PENDIENTE
    @PostMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    public String anular(@PathVariable Long id, RedirectAttributes flash) {
        try {
            ordenCompraService.anular(id);
            flash.addFlashAttribute("exito", "Orden anulada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ordenes/" + id;
    }
}
