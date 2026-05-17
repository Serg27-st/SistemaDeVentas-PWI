package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.ProveedorRequestDTO;
import com.tulicoreria.licoreria.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("proveedores", proveedorService.listarTodos());
        return "proveedores/lista";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN','ALMACEN')")
    public String nuevoForm(Model model) {
        model.addAttribute("proveedor", new ProveedorRequestDTO());
        return "proveedores/formulario";
    }

    @PostMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN','ALMACEN')")
    public String crear(@ModelAttribute ProveedorRequestDTO dto,
                        RedirectAttributes flash) {
        try {
            proveedorService.crear(dto);
            flash.addFlashAttribute("exito", "Proveedor registrado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proveedores";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ALMACEN')")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("proveedor", proveedorService.buscarPorId(id));
        return "proveedores/formulario";
    }

    @PostMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ALMACEN')")
    public String actualizar(@PathVariable Long id,
                             @ModelAttribute ProveedorRequestDTO dto,
                             RedirectAttributes flash) {
        try {
            proveedorService.actualizar(id, dto);
            flash.addFlashAttribute("exito", "Proveedor actualizado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proveedores";
    }

    @PostMapping("/desactivar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            proveedorService.desactivar(id);
            flash.addFlashAttribute("exito", "Proveedor desactivado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proveedores";
    }
}
