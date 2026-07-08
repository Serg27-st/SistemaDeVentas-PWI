package com.tulicoreria.licoreria.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tulicoreria.licoreria.dto.CategoriaRequestDTO;
import com.tulicoreria.licoreria.dto.CategoriaResponseDTO;
import com.tulicoreria.licoreria.service.CategoriaService;

import lombok.RequiredArgsConstructor;

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
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String nuevaForm(Model model) {
        model.addAttribute("categoria", new CategoriaRequestDTO());
        return "categorias/formulario";
    }

    @PostMapping("/nueva")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String crear(@ModelAttribute("categoria") CategoriaRequestDTO dto,
            Model model,
            RedirectAttributes flash) {
        try {
            categoriaService.crear(dto);
            flash.addFlashAttribute("exito", "Categoría creada correctamente");
            return "redirect:/categorias";
        } catch (RuntimeException e) {
            // Mantiene los datos rellenados si algo falla (ej. nombre duplicado)
            model.addAttribute("error", e.getMessage());
            return "categorias/formulario";
        }
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String editarForm(@PathVariable Long id, Model model) {
        CategoriaResponseDTO responseDTO = categoriaService.buscarPorId(id);

        // Mapeo explícito a RequestDTO (con id) porque el formulario de Thymeleaf lo necesita
        CategoriaRequestDTO requestDTO = CategoriaRequestDTO.builder()
                .id(responseDTO.getId())
                .nombre(responseDTO.getNombre())
                .descripcion(responseDTO.getDescripcion())
                .build();

        model.addAttribute("categoria", requestDTO);
        return "categorias/formulario";
    }

    @PostMapping("/editar/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String actualizar(@PathVariable Long id,
            @ModelAttribute("categoria") CategoriaRequestDTO dto,
            Model model,
            RedirectAttributes flash) {
        try {
            categoriaService.actualizar(id, dto);
            flash.addFlashAttribute("exito", "Categoría actualizada correctamente");
            return "redirect:/categorias";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "categorias/formulario";
        }
    }

    @PostMapping("/desactivar/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
