package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.ProveedorRequestDTO;
import com.tulicoreria.licoreria.dto.ProveedorResponseDTO;
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
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ALMACEN')")
    public String nuevoForm(Model model) {
        model.addAttribute("proveedor", new ProveedorRequestDTO());
        return "proveedores/formulario";
    }

    @PostMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ALMACEN')")
    public String crear(@ModelAttribute("proveedor") ProveedorRequestDTO dto,
            Model model,
            RedirectAttributes flash) {
        try {
            proveedorService.crear(dto);
            flash.addFlashAttribute("exito", "Proveedor registrado correctamente");
            return "redirect:/proveedores";
        } catch (RuntimeException e) {
            // Mantiene los datos en el formulario si hay un error (ej: RUC o Correo duplicado)
            model.addAttribute("error", e.getMessage());
            return "proveedores/formulario";
        }
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ALMACEN')")
    public String editarForm(@PathVariable Long id, Model model) {
        ProveedorResponseDTO responseDTO = proveedorService.buscarPorId(id);

        // Mapeo explícito a RequestDTO (con id) porque el formulario de Thymeleaf lo necesita
        ProveedorRequestDTO requestDTO = ProveedorRequestDTO.builder()
                .id(responseDTO.getId())
                .ruc(responseDTO.getRuc())
                .razonSocial(responseDTO.getRazonSocial())
                .telefono(responseDTO.getTelefono())
                .correo(responseDTO.getCorreo())
                .direccion(responseDTO.getDireccion())
                .build();

        model.addAttribute("proveedor", requestDTO);
        return "proveedores/formulario";
    }

    @PostMapping("/editar/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ALMACEN')")
    public String actualizar(@PathVariable Long id,
            @ModelAttribute("proveedor") ProveedorRequestDTO dto,
            Model model,
            RedirectAttributes flash) {
        try {
            proveedorService.actualizar(id, dto);
            flash.addFlashAttribute("exito", "Proveedor actualizado correctamente");
            return "redirect:/proveedores";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "proveedores/formulario";
        }
    }

    @PostMapping("/desactivar/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
