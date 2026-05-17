package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.CategoriaRequestDTO;
import com.tulicoreria.licoreria.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "categorias/lista";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevaForm(Model model) {
        model.addAttribute("categoria", new CategoriaRequestDTO());
        return "categorias/formulario";
    }

    @PostMapping("/nueva")
    @PreAuthorize("hasRole('ADMIN')")
    public String crear(@ModelAttribute CategoriaRequestDTO dto,
                        RedirectAttributes flash) {
        try {
            categoriaService.crear(dto);
            flash.addFlashAttribute("exito", "Categoría creada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categorias";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("categoria", categoriaService.buscarPorId(id));
        return "categorias/formulario";
    }

    @PostMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String actualizar(@PathVariable Long id,
                             @ModelAttribute CategoriaRequestDTO dto,
                             RedirectAttributes flash) {
        try {
            categoriaService.actualizar(id, dto);
            flash.addFlashAttribute("exito", "Categoría actualizada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categorias";
    }

    @PostMapping("/desactivar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            categoriaService.desactivar(id);
            flash.addFlashAttribute("exito", "Categoría desactivada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categorias";
    }
}
