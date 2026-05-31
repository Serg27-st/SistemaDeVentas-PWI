package com.tulicoreria.licoreria.controller;

import com.tulicoreria.licoreria.dto.PromocionRequestDTO;
import com.tulicoreria.licoreria.service.CategoriaService;
import com.tulicoreria.licoreria.service.ProductoService;
import com.tulicoreria.licoreria.service.PromocionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/promociones")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PromocionController {

    private final PromocionService promocionService;
    private final ProductoService  productoService;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("promociones", promocionService.listarTodas());
        return "admin/promociones/lista";
    }

    @GetMapping("/nueva")
    public String nuevaForm(Model model) {
        model.addAttribute("dto", new PromocionRequestDTO());
        model.addAttribute("productos", productoService.listarTodos());
        return "admin/promociones/formulario";
    }

    @PostMapping("/nueva")
    public String crear(@ModelAttribute PromocionRequestDTO dto, RedirectAttributes flash) {
        try {
            promocionService.crear(dto);
            flash.addFlashAttribute("exito", "Promoción creada correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/promociones";
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("dto", promocionService.buscarPorId(id));
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("editando", true);
        model.addAttribute("id", id);
        return "admin/promociones/formulario";
    }

    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Long id,
                             @ModelAttribute PromocionRequestDTO dto,
                             RedirectAttributes flash) {
        try {
            promocionService.actualizar(id, dto);
            flash.addFlashAttribute("exito", "Promoción actualizada.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/promociones";
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        promocionService.desactivar(id);
        flash.addFlashAttribute("exito", "Promoción desactivada.");
        return "redirect:/promociones";
    }
}
