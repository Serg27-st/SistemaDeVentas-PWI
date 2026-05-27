package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.VentaRequestDTO;
import com.tulicoreria.licoreria.service.ClienteService;
import com.tulicoreria.licoreria.service.ProductoService;
import com.tulicoreria.licoreria.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final ProductoService productoService;
    private final ClienteService clienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ventas", ventaService.listarTodas());
        return "ventas/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("venta", ventaService.buscarPorId(id));
        return "ventas/detalle";
    }

    // Punto de venta — formulario principal
    @GetMapping("/nueva")
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public String nuevaForm(Model model) {
        model.addAttribute("ventaRequest", new VentaRequestDTO());
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("clientes", clienteService.listarTodos());
        return "ventas/punto-venta";
    }

    @PostMapping("/nueva")
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public String registrar(@ModelAttribute VentaRequestDTO dto,
                            RedirectAttributes flash) {
        try {
            var venta = ventaService.registrar(dto);
            flash.addFlashAttribute("exito",
                "Venta registrada: " + venta.getNumeroComprobante());
            return "redirect:/ventas/" + venta.getId();
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas/nueva";
        }
    }

    // Ver comprobante para imprimir
    @GetMapping("/{id}/comprobante")
    public String comprobante(@PathVariable Long id, Model model) {
        model.addAttribute("venta", ventaService.buscarPorId(id));
        return "ventas/comprobante";
    }

    // Marcar pedido pendiente como entregado — ADMIN o VENDEDOR
    @PostMapping("/{id}/completar")
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public String completar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            ventaService.completar(id);
            flash.addFlashAttribute("exito", "Pedido marcado como entregado correctamente.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas/" + id;
    }

    // Anular venta — solo ADMIN
    @PostMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    public String anular(@PathVariable Long id, RedirectAttributes flash) {
        try {
            ventaService.anular(id);
            flash.addFlashAttribute("exito", "Venta anulada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas/" + id;
    }
}
